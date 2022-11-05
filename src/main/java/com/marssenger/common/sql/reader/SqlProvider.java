package com.marssenger.common.sql.reader;

import com.marssenger.common.sql.domain.SqlObject;
import com.marssenger.common.sql.exception.NoSqlExistsException;

import java.util.List;
import java.util.Map;

/**
 * @author ligaosheng
 */
public interface SqlProvider {
    /**
     *通过阅读map来获取
     * @throws NoSqlExistsException 不能提供sql时请抛出该异常
     * @return
     */
    List<SqlObject> provide(Map<String,Object> params) throws NoSqlExistsException;
}
