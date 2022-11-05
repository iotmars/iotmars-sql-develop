package com.marssenger.common.sql.domain;


import com.marssenger.common.sql.reader.SqlProvider;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ligaosheng
 */
@Data
public class StandardQueryObject {
    public final static String BEAN = "bean";
    public final static String CLASS = "class";

    /**
     * 提供sql语句
     */
    private SqlProvider sqlProvider;

    /**
     * 环境参数
     */
    private Map<String, Object> params = new HashMap<>();
    /**
     * 处理该请求的类
     */
    private String handler;
    /**
     * 处理该请求的类的类型,bean或者class
     */
    private String handlerType = "bean";

    /**
     * 处理该请求的handler类对应的参数
     */
    private String handlerParams;

    /**
     * 处理该请求的类
     */
    private String executor;
    /**
     * 处理该请求的类的类型,bean或者class
     */
    private String executorType = "bean";

    /**
     * 处理该请求的executor类对应的参数
     */
    private String executorParams;

    /**
     * 返回结果的类型(list/multilist)
     */
    private String resultType;

    /**
     * 方法路由
     */
    private String route;

}
