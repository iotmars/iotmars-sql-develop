package com.marssenger.common.sql.domain;


import lombok.Data;

/**
 * @author Administrator
 * 获取表的元数据
 */
@Data
public class TableSchema {
    private String tableSchema;
    private String tableName;
    private String columnName;
    private String dataType;
}
