package com.marsenger.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.*"})
@MapperScan(basePackages ={"com.marssenger.common.sql.mapper"} )
public class Demo {
    public static void main(String[] args) {
        SpringApplication.run(Demo.class, args);
    }
}

