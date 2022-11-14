package com.marsenger.demo;


import com.marssenger.common.sql.annotation.Route;
import com.marssenger.common.sql.handler.StandardSqlExecuteDispatcherHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Administrator
 */
@Slf4j
@Route(route = "r1", source = "getCacheResult", target = "r1GetCacheResult")
@Route(route = "r1", source = "initEnv", target = "r1InitEnv")
@Route(route = "r2", source = "getCacheResult", target = "r2GetCacheResult")
@Route(route = "r2", source = "initEnv", target = "r2InitEnv")
@Component("myDispatcherHandler")
public class MyDispatcherHandler extends StandardSqlExecuteDispatcherHandler {
    public Object r1GetCacheResult() {
        log.info("enter myGetCacheResult1");
        return null;
    }

    public void r1InitEnv() {
        log.info("enter myInitEnv1");
        getEnv().put("pageSize", "1");
    }

    public Object r2GetCacheResult() {
        log.info("enter myGetCacheResult2");
        return null;
    }

    public void r2InitEnv() {
        log.info("enter myInitEnv2");
        getEnv().put("pageSize", "2");
    }

}
