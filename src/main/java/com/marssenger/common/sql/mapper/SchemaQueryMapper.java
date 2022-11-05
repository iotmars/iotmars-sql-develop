package com.marssenger.common.sql.mapper;

import com.marssenger.common.sql.domain.TableSchema;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 查询表的元数据
 *
 * @author ligaosheng
 */
@Mapper
public interface SchemaQueryMapper {
    /**
     * @param tableSchema
     * @param tableName
     * @return
     */
    @Select("<script>"
            + "SELECT `TABLE_SCHEMA` tableSchema,`TABLE_NAME` tableName,`COLUMN_NAME` columnName,`DATA_TYPE` dataType FROM INFORMATION_SCHEMA.`COLUMNS` WHERE `table_name` = #{tableName} "
            + "<if test=\"tableSchema!=null and tableSchema!='' \">"
            + "  and table_schema=#{tableSchema}"
            + "</if>"
            + "</script>")
    List<TableSchema> getSchemaInfo(@Param("tableSchema") String tableSchema, @Param("tableName") String tableName);
}
