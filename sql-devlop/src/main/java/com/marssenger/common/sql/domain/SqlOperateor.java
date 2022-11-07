package com.marssenger.common.sql.domain;

import java.util.Arrays;
import java.util.List;

/**
 * 比较符号，用于SELECT查询
 * 也可用来根据范式(format)生成sql
 * 里面的符号不区分大小写
 *
 * @author ligaosheng
 */

public enum SqlOperateor {

    /**
     * sql语句的=关键字
     * 例如: a,eq,10
     */
    EqualsTo("EqualsTo", Arrays.asList(new String[]{
            "EQ", "=", "EqualsTo"
    }), "${field},EQ,${value}"),

    /**
     * sql语句的<>关键字
     * 例如: a,_eq,10
     */
    NotEqualsTo("NotEqualsTo", Arrays.asList(new String[]{
            "_EQ", "!EQ", "!=", "NotEqualsTo", "<>"
    }), "${field},_EQ,${value}"),

    /**
     * sql语句的>关键字
     * 例如: a,gt,10
     */
    GreaterThan("GreaterThan", Arrays.asList(new String[]{
            "GT", ">", "GreaterThan", "BiggerThan", "!LE"
    }), "${field},GT,${value}"),

    /**
     * sql语句的>=关键字
     * 例如: a,ge,10
     */
    GreaterThanEquals("GreaterThanEquals", Arrays.asList(new String[]{
            "GE", "gteq", ">=", "GreaterThanEquals", "BiggerThanEquals", "!LT", "_LT"
    }), "${field},GE,${value}"),

    /**
     * sql语句的<关键字
     * 例如: a,lt,10
     */
    MinorThan("MinorThan", Arrays.asList(new String[]{
            "LT", "<", "MinorThan", "SmallerThan", "LessThan", "!GE", "_GE"
    }), "${field},LT,${value}"),

    /**
     * sql语句的<=关键字
     * 例如: a,le,10
     */
    MinorThanEquals("MinorThanEquals", Arrays.asList(new String[]{
            "LE", "lteq", "<=", "MinorThanEquals", "SmallerThanEquals", "LessThanEquals", "!GT", "_GT"
    }), "${field},LE,${value}"),
    /**
     * sql语句的between关键字
     * 例如: a,bt,1,2
     */
    Between("Between", Arrays.asList(new String[]{
            "BT", "Between", "betw", "><", ">=<="
    }), "${field},BT,${value}"),

    /**
     * sql语句的not between关键字
     * 例如: a,_bt,1,2
     */
    NotBetween("NotBetween", Arrays.asList(new String[]{
            "!BT", "NotBetween", "!betw", "!Between", "!><", "!>=<=", "_BT"
    }), "${field},_BT,${value}"),
    /**
     * sql语句的in关键字
     * 例如: a,in,1,2
     */
    In("In", Arrays.asList(new String[]{
            "IN"
    }), "${field},IN,${value}"),

    /**
     * sql语句的not in关键字
     * 例如: a,_in,1,2
     */
    NotIn("NotIn", Arrays.asList(new String[]{
            "_IN", "!IN", "NotIn",
    }), "${field},_IN,${value}"),

    /**
     * sql语句的is null关键字
     * 例如: a,isNull
     */
    IsNull("IsNull", Arrays.asList(new String[]{
            "NL", "IsNull", "null"
    }), "${field},isNull"),

    /**
     * sql语句的is not null关键字
     * 例如: a,_NL
     */
    IsNotNull("IsNotNull", Arrays.asList(new String[]{
            "_NL", "!NL", "IsNotNull", "!null", "!IsNull"
    }), "${field},IsNotNull"),

    /**
     * sql语句的like关键字
     * 例如: a,LK,b*
     */
    Like("Like", Arrays.asList(new String[]{
            "LK", "Like"
    }), "${field},LK,${value}"),

    /**
     * sql语句的not like关键字
     * 例如: a,_LK,b*
     */
    NotLike("NotLike", Arrays.asList(new String[]{
            "_LK", "!LK", "NotLike", "!Like",
    }), "${field},_LK,${value}"),
    ;

    SqlOperateor(String code, List<String> words, String format) {
        this.code = code;
        this.words = words;
    }

    private String code;
    private List<String> words;

    //使用的凡事
    private String format;

    public String getCode() {
        return code;
    }

    public List<String> getWords() {
        return words;
    }

    public String getDefaultWords() {
        return words.get(0);
    }


    public String getFormat() {
        return format;
    }

    public String getFormatValue(String field, String value) {
        String formatValue = format.replace("${field}", field);
        if (value != null) {
            formatValue = formatValue.replace("${value}", value);
        }
        return formatValue.replaceAll("\\s*", "");
    }

    public boolean vagueEquals(String word) {
        boolean isEquals = false;
        if (word != null) {
            for (int i = 0; i < words.size(); i++) {
                if (words.get(i).toUpperCase().equals(word.replaceAll("\\s*", "").toUpperCase())) {
                    isEquals = true;
                    break;
                }
            }
        }
        return isEquals;
    }
}
