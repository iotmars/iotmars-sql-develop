package com.marssenger.common.sql.exception;

/**
 * @author Administrator
 * sql找不到
 */
public class NoSqlExistsException extends Exception {
    public NoSqlExistsException(String s) {
        super(s);
    }
}