package com.marssenger.common.sql.annotation;

import java.lang.annotation.*;

/**
 * @author Administrator
 */
@Repeatable(Routes.class)
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Route {
    /**
     * 路由
     *
     * @return
     */
    String route() default "";

    /**
     * 初始方法
     *
     * @return
     */
    String source() default "";

    /**
     * 目标方法
     *
     * @return
     */
    String target() default "";
}
