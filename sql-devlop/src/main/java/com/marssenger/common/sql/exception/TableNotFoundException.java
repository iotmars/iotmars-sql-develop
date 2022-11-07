package com.marssenger.common.sql.exception;

/**
 * @author ligaosheng
 * 表不存在的异常
 */
public class TableNotFoundException extends RuntimeException {
    public TableNotFoundException(String s) {
        super(s);
    }
}
