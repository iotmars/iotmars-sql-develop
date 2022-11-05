package com.marssenger.common.sql.service;


import com.marssenger.common.sql.domain.StandardQueryObject;
import com.marssenger.common.sql.exception.BeanNotFoundException;
import com.marssenger.common.sql.exception.NoSqlExistsException;

/**
 * @author xieyoujun
 * @version 1.0
 * @date 2022/4/710:57
 **/
public interface StandardSqlService {
    /**
     * 执行sql获取结果
     * @param standardQueryObject
     * @return
     */
    Object getResult(StandardQueryObject standardQueryObject) throws NoSqlExistsException, BeanNotFoundException, ClassNotFoundException;
}
