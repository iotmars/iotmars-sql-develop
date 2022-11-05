package com.marssenger.common.sql.handler;

import com.marssenger.common.sql.domain.SqlObject;
import com.marssenger.common.sql.domain.Result;
import com.marssenger.common.sql.domain.StandardQueryObject;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author ligaosheng
 */
public interface SqlExecuteHandler extends Serializable {

    /**
     * 设置环境的方法
     *
     * @param env
     */
    void setEnv(Map<String, Object> env);

    /**
     * 获取环境的方法
     *
     * @return
     */
    Map<String, Object> getEnv();


    /**
     * 获取handler的参数
     *
     * @return
     */
    String getHandlerParams();

    /**
     * 设置handler的参数
     *
     * @param handlerParams
     */
    void setHandlerParams(String handlerParams);

    /**
     * 初始情况下设置环境变量
     */
    void initEnv();


    /**
     * 初始化sql列表
     * @param list
     */
    List<SqlObject> initSqlList(List<SqlObject> list);


    /**
     * 返回缓存的结果
     *
     * @return 返回的结果
     */
    Object getCacheResult();

    /**
     * 每次执行sql前必然执行的，设置环境变量
     *
     * @param sqlObject sql参数
     */
    void setEnvBeforeExecuteEachSql(SqlObject sqlObject);

    /**
     * 每次执行sql后必然执行的
     *
     * @param sqlObject sql参数
     * @param result    本次执行的结果
     */
    public void setEnvAfterExecuteEachSql(SqlObject sqlObject, Result result);

    /**
     * 对最终执行的sql进行处理
     *
     * @param standardQueryObject 初始的查询参数
     * @param sqlObjects          sql列表
     * @param resultMap           对象列表
     * @return 最终的返回结果
     */
    public Object setFinalResult(StandardQueryObject standardQueryObject, List<SqlObject> sqlObjects, Map<String, Result> resultMap);

}
