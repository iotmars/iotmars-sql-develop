package com.marssenger.common.sql.executor;

import com.marssenger.common.sql.domain.CommandObject;
import com.marssenger.common.sql.domain.SqlObject;
import com.marssenger.common.sql.domain.Result;
import com.marssenger.common.sql.handler.SqlExecuteHandler;

import java.util.Map;

/**
 * sql执行器
 */
public interface SqlExecutor {

    /**
     * 初始化方法，一般是根据executorParams做初始化
     */
    void init();

    /**
     * 设置handler
     *
     * @param sqlExecuteHandler
     */
    void setSqlExecuteHandler(SqlExecuteHandler sqlExecuteHandler);

    /**
     * 获取handler
     *
     * @return
     */
    SqlExecuteHandler getSqlExecuteHandler();

    /**
     * 获取执行器参数
     *
     * @return
     */
    String getExecutorParams();

    /**
     * 设置执行器参数
     *
     * @param executorParams
     */
    void setExecutorParams(String executorParams);


    /**
     * 执行一个sql,获取结果
     *
     * @param env       运行环境
     * @param sqlObject 当前执行的SQL对象
     * @return
     */
    Result execute(Map<String, Object> env, SqlObject sqlObject);

    /**
     * 执行一个命令
     * @param commandObject
     * @return
     */
    Object doCommand(CommandObject commandObject);

}
