package com.marssenger.common.sql.handler;

/**
 * @author ligaosheng
 */
public interface SqlExecuteDispatcherHandler extends SqlExecuteHandler {
    /**
     * 获取路由
     * @return
     */
    public String getRoute();

    /**
     * 设置路由
     * @param route
     */
    public void setRoute(String route);
}
