package com.marssenger.common.sql.domain;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;



/**
 * @author ligaosheng
 */
@Data
@Slf4j
@ToString
public class SqlObject extends CommandObject {

    /**
     * 该SqlResult的标识符
     */
    private String identifier;
    /**
     * SQL关键字
     */
    private String sqlCode;
    /**
     * SQL类型
     */
    private String sqlType;
    /**
     * 具体的sql
     */
    private String sql;

    /**
     * 求总数的sql
     */
    private String totalSql;

    /**
     * 具体的sql返回结果的名字
     */
    private String sqlResultName;

    /**
     * 求总数的sql的返回结果的名字
     */
    private String totalSqlResultName;

    /**
     * jexl表达式
     */
    private String jexl;

    /**
     * 上下文运行环境
     */
    private String map;

    /**
     * 备注
     */
    private String remark;

    /**
     * sql所属的系列
     */
    private String seriesCode;

    /**
     * 排序
     */
    private String orderNo;


    public String getPerfectSqlResultName() {
        if (sqlResultName == null || sqlResultName.trim().length() == 0) {
            return sqlCode + "Result";
        } else {
            return sqlResultName.trim();
        }
    }

    public String getPerfectTotalSqlResultName() {
        if (totalSqlResultName == null || totalSqlResultName.trim().length() == 0) {
            return sqlCode + "Count";
        } else {
            return totalSqlResultName.trim();
        }
    }

    public JSONObject getJsonMap() {
        try {
            if (this.map == null || this.map.trim().length() == 0) {
                return new JSONObject();
            } else {
                return JSONObject.parseObject(this.map.trim());
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
            return new JSONObject();
        }
    }

    public void setJsonMap(JSONObject jsonObject) {
        this.map = jsonObject.toJSONString();
    }

}
