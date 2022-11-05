package com.marssenger.common.sql.exception;

/**
 * @author ligaosheng
 * bean不存在
 */
public class BeanNotFoundException extends Exception {
    public BeanNotFoundException(String message) {
        super(message);
    }
}
