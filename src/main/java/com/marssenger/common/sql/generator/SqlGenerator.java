package com.marssenger.common.sql.generator;

import com.marssenger.common.sql.domain.SqlObject;
import com.marssenger.common.sql.domain.SqlType;

import java.util.Map;

/**
 * 一个自动化的sql生成语句(基于JSQLPARSER进行SQL解析)
 *
 * @author ligaosheng
 */
public interface SqlGenerator {
    /**
     * 根据sqlType和params生成对应的sql语句
     * @param sqlType sql类型
     * @param params  参数类型
     * @return SqlObject
     */
    SqlObject generate(SqlType sqlType, Map<String, Object> params);
}
