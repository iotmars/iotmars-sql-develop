package com.marssenger.common.sql.domain;

/**
 * SQL类型的枚举
 *
 * @author Administrator
 */

public enum SqlType {
    SELECT_ONE_TABLE("R1", "单表查询"),
    SELECT_LOTS_TABLE("R2", "多表查询"),
    ;
    SqlType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    private String code;
    private String desc;
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
}
