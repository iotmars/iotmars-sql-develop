package com.marssenger.common.sql.annotation;

import java.lang.annotation.*;

/**
 * @author Administrator
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Routes {
    Route[] value();
}
