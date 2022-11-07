package com.marssenger.common.sql.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.marssenger.common.sql.exception.BeanNotFoundException;
import com.marssenger.common.sql.exception.NoSqlExistsException;
import com.marssenger.common.sql.executor.SqlExecutor;
import com.marssenger.common.sql.executor.StandardSqlExecutor;
import com.marssenger.common.sql.handler.SqlExecuteHandler;
import com.marssenger.common.sql.domain.SqlObject;
import com.marssenger.common.sql.domain.Result;
import com.marssenger.common.sql.domain.StandardQueryObject;
import com.marssenger.common.sql.handler.StandardSqlExecuteDispatcherHandler;
import com.marssenger.common.sql.handler.StandardSqlExecuteHandler;
import com.marssenger.common.sql.service.StandardSqlService;
import com.marssenger.common.sql.utils.CopyUtils;
import com.marssenger.common.sql.utils.SpringUtilsForSql;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.jexl3.internal.Engine;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * @author ligaosheng
 */
@Service
@Slf4j
public class StandardSqlServiceImpl implements StandardSqlService {

    public final static String PARAMS = "params";
    public final static String SQL_RESULT = "sqlResult";
    public String spilter = "========================================================================================================================================================================";

    /**
     * 做开始结尾处理
     *
     * @param standardQueryObject
     * @return
     */
    @Override
    public Object getResult(StandardQueryObject standardQueryObject) throws NoSqlExistsException, BeanNotFoundException, ClassNotFoundException {
        Map<String, Object> map = standardQueryObject.getParams();
        String handlerClass = standardQueryObject.getHandler();
        String handlerType = standardQueryObject.getHandlerType();
        SqlExecuteHandler handler;
        String route = standardQueryObject.getRoute();
        if (handlerClass != null && handlerClass.trim().length() > 0) {
            if (StandardQueryObject.BEAN.equals(handlerType)) {
                try {
                    Object bean = SpringUtilsForSql.getBean(handlerClass);
                    Class aClass = bean.getClass();
                    String newBeanStr = JSONObject.toJSONString(bean);
                    Object newBean = JSONObject.parseObject(newBeanStr, aClass);
                    CopyUtils.copy(bean, newBean);
                    handler = (SqlExecuteHandler) newBean;
                } catch (Exception e) {
                    throw new BeanNotFoundException(handlerClass + " bean not found");
                }
            } else {
                try {
                    handlerClass = handlerClass.replace("_", ".");
                    Class aClass = Class.forName(handlerClass);
                    Object instance = aClass.newInstance();
                    handler = (SqlExecuteHandler) instance;
                } catch (Exception e) {
                    throw new ClassNotFoundException(handlerClass + " class not found");
                }
            }
        } else {
            if (route != null && route.trim().length() > 0) {
                handler = new StandardSqlExecuteDispatcherHandler();

            } else {
                handler = new StandardSqlExecuteHandler();
            }
        }

        if (handler instanceof StandardSqlExecuteDispatcherHandler) {
            ((StandardSqlExecuteDispatcherHandler) handler).setRoute(route);
        }

        String handlerParams = standardQueryObject.getHandlerParams();
        //设置环境变量
        handler.setHandlerParams(handlerParams);
        setStandardEnv(map);
        handler.setEnv(map);
        handler.initEnv();
        String executorClass = standardQueryObject.getExecutor();
        String executorType = standardQueryObject.getExecutorType();
        SqlExecutor executor;
        if (executorClass != null && executorClass.trim().length() > 0) {
            if (StandardQueryObject.BEAN.equals(executorType)) {
                try {
                    Object bean = SpringUtilsForSql.getBean(executorClass);
                    Class aClass = bean.getClass();
                    String newBeanStr = JSONObject.toJSONString(bean);
                    Object newBean = JSONObject.parseObject(newBeanStr, aClass);
                    CopyUtils.copy(bean, newBean);
                    executor = (SqlExecutor) newBean;
                } catch (Exception e) {
                    throw new BeanNotFoundException(executorClass + " bean not found");
                }
            } else {
                try {
                    executorClass = executorClass.replace("_", ".");
                    Class aClass = Class.forName(executorClass);
                    Object instance = aClass.newInstance();
                    executor = (SqlExecutor) instance;
                } catch (Exception e) {
                    throw new ClassNotFoundException(executorClass + " class not found");
                }
            }
        } else {
            executor = new StandardSqlExecutor();
        }
        String executorParams = standardQueryObject.getExecutorParams();
        executor.setExecutorParams(executorParams);
        executor.setSqlExecuteHandler(handler);
        executor.init();
        Object resultObject = handler.getCacheResult();
        if (resultObject != null) {
            return resultObject;
        }
        //注意,handler.initEnv();执行在方法provide()之前,这也就意味着sql里面的参数可以更动态化，provide()可以使用动态的上下文的参数。
        List<SqlObject> sqlObjects = standardQueryObject.getSqlProvider().provide(handler.getEnv());
        if (sqlObjects == null || sqlObjects.size() == 0) {
            throw new NoSqlExistsException("没有sql可以执行");
        }
        Map<String, Result> resultMap = new HashMap<>();
        StringBuffer sb = new StringBuffer();
        sb.append(spilter + "\n");
        sb.append("Sql执行计划:\n");
        for (int i = 0; i < sqlObjects.size(); i++) {
            sb.append("step" + i + ":" + sqlObjects.get(i).getSqlCode() + "(" + sqlObjects.get(i).getSqlType() + ")");
        }
        sb.append(spilter);
        log.info(sb.toString());
        List<SqlObject> retSqlObjects = handler.initSqlList(sqlObjects);
        if (retSqlObjects != null) {
            sqlObjects = retSqlObjects;
        }
        for (int i = 0; i < sqlObjects.size(); i++) {
            long bms = System.currentTimeMillis();
            SqlObject sqlObject = sqlObjects.get(i);
            handler.setEnvBeforeExecuteEachSql(sqlObject);
            Result result = executor.execute(handler.getEnv(), sqlObject);
            String identifier = sqlObject.getIdentifier();
            if (identifier == null || identifier.trim().length() == 0) {
                identifier = "$" + i;
                sqlObject.setIdentifier(identifier);
            }
            result.setIdentifier(sqlObject.getIdentifier());
            resultMap.put(result.getIdentifier(), result);
            if (!StringUtils.isEmpty(sqlObject.getSqlCode())) {
                handler.getEnv().put(sqlObject.getPerfectSqlResultName(), result.getSqlResult());
                handler.getEnv().put(sqlObject.getPerfectTotalSqlResultName(), result.getTotalSqlResult());
            }
            handler.getEnv().put(result.getIdentifier(), result);
            if (!StringUtils.isEmpty(sqlObject.getCommandName())) {
                handler.getEnv().put(sqlObject.getCommandName(), result.getCommandResult());
            }
            handler.setEnvAfterExecuteEachSql(sqlObject, result);
            Object obj = setResultWithJexl(result, handler.getEnv(), sqlObject.getJexl());
            if (obj != null && obj instanceof Result) {
                result = (Result) obj;
            }
            if (!StringUtils.isEmpty(sqlObject.getSqlCode())) {
                handler.getEnv().put(sqlObject.getPerfectSqlResultName(), result.getSqlResult());
                handler.getEnv().put(sqlObject.getPerfectTotalSqlResultName(), result.getTotalSqlResult());
            }
            if (!StringUtils.isEmpty(result.getIdentifier())) {
                handler.getEnv().put(result.getIdentifier(), result);
                resultMap.put(result.getIdentifier(), result);
            }
            long cms = System.currentTimeMillis();
            log.info("sqlCode=" + sqlObject.getSqlCode() + ",one call cost:" + (cms - bms) + "ms");
        }
        resultMap.entrySet().forEach(e -> {
            StandardSqlExecuteHandler.underlineToCamel(e.getValue());
            StandardSqlExecuteHandler.parseDataType(e.getValue());
        });
        return handler.setFinalResult(standardQueryObject, sqlObjects, resultMap);
    }


    //设置标准动作
    public static void setStandardEnv(Map<String, Object> env) {
        setPageEnv(env);
    }

    //设置分页
    public static void setPageEnv(Map<String, Object> env) {
        Integer pageNum = null;
        Integer pageSize = null;
        if (env.get("pageNum") != null) {
            pageNum = Integer.parseInt(env.get("pageNum").toString());
        }
        if (env.get("pageSize") != null) {
            pageSize = Integer.parseInt(env.get("pageSize").toString());
        }
        if (pageNum != null && pageSize != null && pageNum >= 1) {
            Integer startRow = (pageNum - 1) * pageSize;
            env.put("_startRow", startRow);
            env.put("_start", startRow);
        }
    }


    /**
     * 对单次结果进行设置,(运行jexl表达式)
     *
     * @param result
     * @param params
     * @param jexl
     */
    public static Object setResultWithJexl(Result result, Map<String, Object> params, String jexl) {
        if (jexl != null && jexl.trim().length() > 0) {
            JexlEngine engine = new Engine();
            JexlContext context = new MapContext();
            params.forEach((k, v) -> {
                context.set(k, v);
            });
            context.set(SQL_RESULT, result);
            context.set(PARAMS, params);
            JexlExpression expression = engine.createExpression(jexl);
            Object evaluate = expression.evaluate(context);
            return evaluate;
        }
        return null;
    }

}