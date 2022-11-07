package com.marssenger.common.sql.controller;


import com.marssenger.common.sql.domain.StandardQueryObject;
import com.marssenger.common.sql.reader.DatabaseSqlProvider;
import com.marssenger.common.sql.reader.R1SqlProvider;
import com.marssenger.common.sql.reader.R2SqlProvider;
import com.marssenger.common.sql.service.StandardSqlService;
import com.marssenger.common.sql.utils.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author ligaosheng
 * @version 1.0
 * @date 2022/4/710:57
 **/
@RestController
@RequestMapping("/common/standard/sql")
public class StandardController {

    @Autowired
    private StandardSqlService cardListService;


    /**
     * 复杂的查询
     *
     * @param sqlCode
     * @param seriesCode
     * @param params
     * @param handler
     * @param handlerType
     * @param handlerParams
     * @param executor
     * @param executorType
     * @param executorParams
     * @param resultType
     * @param route
     * @return
     */
    @RequestMapping("/list")
    public Object getList(@RequestParam(required = false) String sqlCode, @RequestParam(required = false) String seriesCode, @RequestParam Map<String, Object> params,
                          @RequestParam(required = false) String handler, @RequestParam(defaultValue = StandardQueryObject.BEAN) String handlerType, @RequestParam(required = false) String handlerParams,
                          @RequestParam(required = false) String executor, @RequestParam(defaultValue = StandardQueryObject.BEAN) String executorType, @RequestParam(required = false) String executorParams,
                          @RequestParam(defaultValue = "list") String resultType, @RequestParam(required = false) String route) {
        if (StringUtils.isEmpty(sqlCode) && StringUtils.isEmpty(seriesCode)) {
            throw new RuntimeException("sqlCode和seriesCode不能同时为空");
        }
        StandardQueryObject standardQueryObject = new StandardQueryObject();
        standardQueryObject.setParams(params);
        DatabaseSqlProvider provider = new DatabaseSqlProvider();
        provider.setSqlCode(sqlCode);
        provider.setSeriesCode(seriesCode);
        standardQueryObject.setSqlProvider(provider);
        standardQueryObject.setResultType(resultType);
        standardQueryObject.setHandler(handler);
        standardQueryObject.setHandlerType(handlerType);
        standardQueryObject.setHandlerParams(handlerParams);
        standardQueryObject.setExecutor(executor);
        standardQueryObject.setExecutorType(executorType);
        standardQueryObject.setExecutorParams(executorParams);
        standardQueryObject.setRoute(route);
        try {
            Object result = cardListService.getResult(standardQueryObject);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 单表查询
     *
     * @param params
     * @param handler
     * @param handlerType
     * @param handlerParams
     * @param executor
     * @param executorType
     * @param executorParams
     * @param resultType
     * @param route
     * @return
     */
    @RequestMapping("/selectOneTable")
    public Object selectOneTable(
            @RequestParam Map<String, Object> params,
            @RequestParam(required = false) String handler, @RequestParam(defaultValue = StandardQueryObject.BEAN) String handlerType, @RequestParam(required = false) String handlerParams,
            @RequestParam(required = false) String executor, @RequestParam(defaultValue = StandardQueryObject.BEAN) String executorType, @RequestParam(required = false) String executorParams,
            @RequestParam(defaultValue = "list") String resultType, @RequestParam(required = false) String route) {
        R1SqlProvider r1SqlProvider = new R1SqlProvider();
        StandardQueryObject standardQueryObject = new StandardQueryObject();
        standardQueryObject.setParams(params);
        standardQueryObject.setSqlProvider(r1SqlProvider);
        standardQueryObject.setResultType(resultType);
        standardQueryObject.setHandler(handler);
        standardQueryObject.setHandlerType(handlerType);
        standardQueryObject.setHandlerParams(handlerParams);
        standardQueryObject.setExecutor(executor);
        standardQueryObject.setExecutorType(executorType);
        standardQueryObject.setExecutorParams(executorParams);
        standardQueryObject.setRoute(route);
        try {
            Object result = cardListService.getResult(standardQueryObject);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }


    /**
     * 多表查询
     *
     * @param params
     * @param handler
     * @param handlerType
     * @param handlerParams
     * @param executor
     * @param executorType
     * @param executorParams
     * @param resultType
     * @param route
     * @return
     */
    @RequestMapping("/commonSelect")
    public Object commonSelect(
            @RequestParam Map<String, Object> params,
            @RequestParam(required = false) String handler, @RequestParam(defaultValue = StandardQueryObject.BEAN) String handlerType, @RequestParam(required = false) String handlerParams,
            @RequestParam(required = false) String executor, @RequestParam(defaultValue = StandardQueryObject.BEAN) String executorType, @RequestParam(required = false) String executorParams,
            @RequestParam(defaultValue = "list") String resultType, @RequestParam(required = false) String route) {
        R2SqlProvider r2SqlProvider = new R2SqlProvider();
        StandardQueryObject standardQueryObject = new StandardQueryObject();
        standardQueryObject.setParams(params);
        standardQueryObject.setSqlProvider(r2SqlProvider);
        standardQueryObject.setResultType(resultType);
        standardQueryObject.setHandler(handler);
        standardQueryObject.setHandlerType(handlerType);
        standardQueryObject.setHandlerParams(handlerParams);
        standardQueryObject.setExecutor(executor);
        standardQueryObject.setExecutorType(executorType);
        standardQueryObject.setExecutorParams(executorParams);
        standardQueryObject.setRoute(route);
        try {
            Object result = cardListService.getResult(standardQueryObject);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return AjaxResult.error(e.getMessage());
        }
    }


}
