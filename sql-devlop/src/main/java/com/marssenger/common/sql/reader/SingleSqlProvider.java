package com.marssenger.common.sql.reader;

import com.marssenger.common.sql.domain.SqlObject;
import com.marssenger.common.sql.exception.NoSqlExistsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 提供单一的SQL
 */
public class SingleSqlProvider implements SqlProvider  {

    private List<SqlObject> sqls;

    public SingleSqlProvider(SqlObject sqlObject){
        sqls=new ArrayList<>();
        sqls.add(sqlObject);
    }

    @Override
    public List<SqlObject> provide(Map<String,Object> params) throws NoSqlExistsException {
        return sqls;
    }
}
