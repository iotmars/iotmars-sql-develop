package com.marsenger.demo;


import com.marssenger.common.sql.annotation.Route;
import com.marssenger.common.sql.handler.StandardSqlExecuteDispatcherHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Administrator
 */
@Slf4j
@Route(route = "r1", source = "getCacheResult", target = "myGetCacheResult")
@Route(route = "r1", source = "initEnv", target = "myInitEnv")
@Component("myDispatcherHandler")
public class MyDispatcherHandler extends StandardSqlExecuteDispatcherHandler {
    public Object myGetCacheResult() {
        log.info("entered");
        return null;
    }

    public void myInitEnv() {
        getEnv().put("pageNum1", "2");
    }

}
