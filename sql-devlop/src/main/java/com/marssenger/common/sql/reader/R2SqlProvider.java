package com.marssenger.common.sql.reader;

import com.marssenger.common.sql.domain.SqlObject;
import com.marssenger.common.sql.exception.NoSqlExistsException;
import com.marssenger.common.sql.generator.JSqlGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author ligaosheng
 */
public class R2SqlProvider implements SqlProvider{
    @Override
    public List<SqlObject> provide(Map<String, Object> params) throws NoSqlExistsException {
        SqlObject sqlObject = JSqlGenerator.getR2Object(params);
        return Arrays.asList(sqlObject);
    }
}
