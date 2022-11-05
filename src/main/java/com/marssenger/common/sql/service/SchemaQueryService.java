package com.marssenger.common.sql.service;

import com.marssenger.common.sql.domain.TableSchema;

import java.util.List;

/**
 * 查询标的元数据
 * @author ligaosheng
 */
public interface SchemaQueryService {

    /**
     *
     * @param tableSchema
     * @param tableName
     * @return
     */
    List<TableSchema> getSchemaInfo(String tableSchema, String tableName);

}
