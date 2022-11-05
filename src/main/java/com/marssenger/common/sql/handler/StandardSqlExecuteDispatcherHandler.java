package com.marssenger.common.sql.handler;


import com.marssenger.common.sql.annotation.Route;
import com.marssenger.common.sql.annotation.Routes;
import com.marssenger.common.sql.domain.Result;
import com.marssenger.common.sql.domain.SqlObject;
import com.marssenger.common.sql.domain.StandardQueryObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author ligaosheng
 */
public class StandardSqlExecuteDispatcherHandler implements SqlExecuteDispatcherHandler {

    public static final String setEnv = "setEnv";
    public static final String setHandlerParams = "setHandlerParams";
    public static final String initEnv = "initEnv";
    public static final String initSqlList = "initSqlList";
    public static final String getCacheResult = "getCacheResult";
    public static final String setEnvBeforeExecuteEachSql = "setEnvBeforeExecuteEachSql";
    public static final String setEnvAfterExecuteEachSql = "setEnvAfterExecuteEachSql";
    public static final String setFinalResult = "setFinalResult";
    private String route;

    private Map<String, Object> env;

    private String handlerParams;

    @Override
    public String getRoute() {
        return route;
    }

    @Override
    public void setRoute(String route) {
        this.route = route;
    }

    @Override
    public void setEnv(Map<String, Object> env) {
        this.env = env;
    }

    @Override
    public Map<String, Object> getEnv() {
        return env;
    }

    @Override
    public String getHandlerParams() {
        return handlerParams;
    }

    @Override
    public void setHandlerParams(String handlerParams) {
        this.handlerParams = handlerParams;
    }

    @Override
    public void initEnv() {
        String targetMethodName = getMethodName(initEnv);
        if (targetMethodName != null && targetMethodName.length() > 0) {
            invokeMethod(this, targetMethodName);
        }
    }

    @Override
    public List<SqlObject> initSqlList(List<SqlObject> list) {
        String targetMethodName = getMethodName(initSqlList);
        if (targetMethodName != null && targetMethodName.length() > 0) {
            return (List<SqlObject>) invokeMethod(this, targetMethodName);
        } else {
            return null;
        }
    }

    @Override
    public Object getCacheResult() {
        String targetMethodName = getMethodName(getCacheResult);
        if (targetMethodName != null && targetMethodName.length() > 0) {
            return invokeMethod(this, targetMethodName);
        } else {
            return null;
        }
    }

    @Override
    public void setEnvBeforeExecuteEachSql(SqlObject sqlObject) {
        String targetMethodName = getMethodName(setEnvBeforeExecuteEachSql);
        if (targetMethodName != null && targetMethodName.length() > 0) {
            invokeMethod(this, targetMethodName, sqlObject);
        }
    }

    @Override
    public void setEnvAfterExecuteEachSql(SqlObject sqlObject, Result result) {
        String targetMethodName = getMethodName(setEnvAfterExecuteEachSql);
        if (targetMethodName != null && targetMethodName.length() > 0) {
            invokeMethod(this, targetMethodName, sqlObject, result);
        }
    }

    @Override
    public Object setFinalResult(StandardQueryObject standardQueryObject, List<SqlObject> sqlObjects, Map<String, Result> resultMap) {
        String targetMethodName = getMethodName(setFinalResult);
        if (targetMethodName != null && targetMethodName.length() > 0) {
            return invokeMethod(this, targetMethodName, standardQueryObject, sqlObjects, resultMap);
        } else {
            return StandardSqlExecuteHandler.defaultSetFinalResult(this, standardQueryObject, sqlObjects, resultMap);
        }
    }

    public Object invokeMethod(Object obj, String methodName, Object... args) {
        try {
            Method[] methods = this.getClass().getDeclaredMethods();
            Method targetMethod = null;
            for (Method m : methods) {
                if (m.getName().equals(methodName)) {
                    Class<?>[] parameterTypes = m.getParameterTypes();
                    if (args.length == parameterTypes.length) {
                        boolean isThis = true;
                        for (int i = 0; i < args.length; i++) {
                            if (!args.getClass().getName().equals(parameterTypes[i].getClass().getName())) {
                                isThis = false;
                            }
                        }
                        if (isThis) {
                            targetMethod = m;
                            break;
                        }
                    }
                }
            }
            return targetMethod.invoke(obj, args);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getMethodName(String sourceMethodName) {
        String targetMethodName = null;
        if (route != null && route.length() > 0) {
            if (sourceMethodName != null && sourceMethodName.length() > 0) {
                for (Annotation annotation : this.getClass().getAnnotations()) {
                    if (annotation instanceof Routes) {
                        Route[] ros = ((Routes) annotation).value();
                        if (ros != null) {
                            for (Route ro : ros) {
                                String thisRoute = ro.route();
                                String thisSourceMethodName = ro.source();
                                String thisTargetMethodName = ro.target();
                                if (route.equals(thisRoute)) {
                                    if (thisSourceMethodName != null && thisSourceMethodName.length() > 0 && thisTargetMethodName != null && thisTargetMethodName.length() > 0) {
                                        if (sourceMethodName.equals(thisSourceMethodName)) {
                                            targetMethodName = thisTargetMethodName;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return targetMethodName;
    }


}
