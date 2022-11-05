package com.marssenger.common.sql.reader;

import com.marssenger.common.sql.domain.SqlObject;
import com.marssenger.common.sql.exception.NoSqlExistsException;
import com.marssenger.common.sql.mapper.StandardSqlMapper;
import com.marssenger.common.sql.utils.SpringUtilsForSql;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
public class DatabaseSqlProvider implements SqlProvider {

    public static final String SQLCODE ="sqlCode";
    public static final String SERIESCODE ="seriesCode";

    private String sqlCode;
    private String seriesCode;

    public String getSqlCode() {
        return sqlCode;
    }

    public void setSqlCode(String sqlCode) {
        this.sqlCode = sqlCode;
    }

    public String getSeriesCode() {
        return seriesCode;
    }

    public void setSeriesCode(String seriesCode) {
        this.seriesCode = seriesCode;
    }

    /**
     *
     * @param params
     * @return
     * @throws NoSqlExistsException
     */
    @Override
    public List<SqlObject> provide(Map<String,Object> params) throws NoSqlExistsException {

        if(params!=null){
            if(params.get(SQLCODE)!=null){
                this.sqlCode=params.get(SQLCODE)+"";
            }
            if(params.get(SERIESCODE)!=null){
                this.seriesCode=params.get(SERIESCODE)+"";
            }
        }

        StandardSqlMapper cardListMapper = SpringUtilsForSql.getBean(StandardSqlMapper.class);
        List<SqlObject> sqlObjects = new ArrayList<>();
        if (StringUtils.isEmpty(sqlCode)) {
            if(StringUtils.isEmpty(seriesCode)){
                throw new NoSqlExistsException("sqlCode和seriesCode不能同事为空");
            }
            List<SqlObject> sqlBySeriesCode = cardListMapper.getSqlBySeriesCode(seriesCode);
            if (sqlBySeriesCode == null || sqlBySeriesCode.size() == 0) {
                throw new NoSqlExistsException("根据seriesCode无法查询到sql");
            }
            sqlObjects = sqlBySeriesCode;
        } else {
            SqlObject sqlObject = cardListMapper.getSql(sqlCode);
            if (sqlObject == null) {
                throw new NoSqlExistsException("根据sqlCode无法查询到sql");
            }
            sqlObjects.add(sqlObject);
        }
        return sqlObjects;
    }

}
