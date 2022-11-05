package com.marssenger.common.sql.domain;


import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ligaosheng
 */
@Data
public class Result {
    /**
     * 该SqlResult的标识符
     */
    private  String identifier;
    /**
     * 下一个要执行的SQL
     */
    private String sqlCode;
    /**
     * 查询操作的结果
     */
    private List<Map<String, Object>> selectResult;

    /**
     * INSERT操作的结果
     */
    private Integer insertResult;
    /**
     * UPDATE操作的结果
     */
    private Integer updateResult;
    /**
     * DELETE操作的结果
     */
    private Integer deleteResult;

    /**
     * 通常结果
     */
    private Object sqlResult;

    /**
     * 记录总条数(适用于分页)
     */
    private Integer totalSqlResult;


    /**
     *
     */
    private Object CommandResult;

    /**
     * 当前环境
     */
    private Map<String, Object> envMap = new HashMap<>();
}
