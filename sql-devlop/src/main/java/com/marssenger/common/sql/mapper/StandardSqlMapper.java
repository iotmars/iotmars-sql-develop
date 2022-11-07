package com.marssenger.common.sql.mapper;

import com.marssenger.common.sql.domain.SqlObject;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author xieyoujun
 * @version 1.0
 * @date 2022/4/710:57
 **/
@Mapper
public interface StandardSqlMapper {

    String alias = "sql_code identifier,sql_code sqlCode,sql_type sqlType,`sql` `sql`,total_sql totalSql,sql_result_name sqlResultName,total_sql_result_name totalSqlResultName,jexl jexl ,map map ,remark remark";
    /**
     * 执行插入
     *
     * @param map 当前环境
     * @return
     */
    @InsertProvider(StandardSqlProvider.class)
    Integer insert(@Param("params") Map<String, Object> map);

    /**
     * 执行删除
     *
     * @param map 当前环境
     * @return
     */
    @DeleteProvider(StandardSqlProvider.class)
    Integer delete(@Param("params") Map<String, Object> map);

    /**
     * 执行更新
     *
     * @param map 当前环境
     * @return
     */
    @UpdateProvider(StandardSqlProvider.class)
    Integer update(@Param("params") Map<String, Object> map);

    /**
     * 执行查询
     *
     * @param map 当前环境
     * @return
     */
    @SelectProvider(StandardSqlProvider.class)
    List<Map<String, Object>> select(@Param("params") Map<String, Object> map);


    /**
     * 获取分页的总数
     *
     * @param map 当前环境
     * @return
     */
    @SelectProvider(StandardSqlProvider.class)
    Integer selectCount(@Param("params") Map<String, Object> map);

    /**
     * 获取sql
     *
     * @param sqlCode sql代码
     * @return
     */
    @Select("select  " + alias + " from cc_base_sql where sql_code = #{sqlCode}")
    SqlObject getSql(@Param("sqlCode") String sqlCode);

    /**
     * 根据系列号获取关联的sql列表
     *
     * @param seriesCode 系列CODE
     * @return
     */
    @Select("SELECT IFNULL(a.identifier,a.id) identifier ,a.series_code seriesCode,a.order_no orderNo,b.sql_code sqlCode,b.sql_type sqlType,b.`sql` `sql`,b.total_sql totalSql,b.sql_result_name sqlResultName,b.total_sql_result_name totalSqlResultName,b.jexl jexl ,b.map map ,b.remark remark FROM cc_base_sql_series a inner join cc_base_sql b on a.sql_code=b.sql_code  WHERE a.series_code = #{seriesCode} order by a.order_no")
    List<SqlObject> getSqlBySeriesCode(String seriesCode);

}
