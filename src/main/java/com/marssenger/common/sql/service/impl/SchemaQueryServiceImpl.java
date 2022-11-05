package com.marssenger.common.sql.service.impl;

import com.marssenger.common.sql.domain.TableSchema;
import com.marssenger.common.sql.mapper.SchemaQueryMapper;
import com.marssenger.common.sql.service.SchemaQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SchemaQueryServiceImpl  implements SchemaQueryService {

    @Autowired
    SchemaQueryMapper schemaQueryMapper;

    @Override
    public List<TableSchema> getSchemaInfo(String tableSchema, String tableName) {
        return schemaQueryMapper.getSchemaInfo(tableSchema,tableName);
    }
}
