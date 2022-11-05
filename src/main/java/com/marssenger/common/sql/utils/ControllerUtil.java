package com.marssenger.common.sql.utils;

import com.marssenger.common.sql.domain.StandardQueryObject;
import com.marssenger.common.sql.exception.BeanNotFoundException;
import com.marssenger.common.sql.exception.NoSqlExistsException;
import com.marssenger.common.sql.service.StandardSqlService;


/**
 * @author Administrator
 */
public class ControllerUtil {
    public static Object getResult(StandardQueryObject standardQueryObject) throws NoSqlExistsException, BeanNotFoundException, ClassNotFoundException {
        StandardSqlService cardListService = SpringUtilsForSql.getBean(StandardSqlService.class);
        Object result = cardListService.getResult(standardQueryObject);
        return result;
    }
}
