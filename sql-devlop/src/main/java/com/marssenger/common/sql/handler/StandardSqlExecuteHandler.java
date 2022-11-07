package com.marssenger.common.sql.handler;

import com.marssenger.common.sql.domain.SqlObject;
import com.marssenger.common.sql.domain.Result;
import com.marssenger.common.sql.domain.StandardQueryObject;
import com.marssenger.common.sql.exception.NoSqlExistsException;
import com.marssenger.common.sql.executor.StandardSqlExecutor;
import com.marssenger.common.sql.reader.DatabaseSqlProvider;
import com.marssenger.common.sql.utils.StringCamelUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ligaosheng
 */
@Slf4j
public class StandardSqlExecuteHandler implements SqlExecuteHandler {

    public final static String RESULT_TYPE_LIST = "list";

    public final static String RESULT_TYPE_RAWLIST = "rawlist";

    public final static String RESULT_TYPE_MULTILIST = "multilist";

    public final static String RESULT_TYPE_MULTISUM_ONE_LIST = "multiSumInfoAndOneList";

    public final static String MULTILIST_AND_ONELIST = "multiListAndOneList";

    //初始化环境变量的SQL
    public final static String initSqlCode = "initSqlCode";


    private Map<String, Object> env;

    private String handlerParams;

    @Override
    public void setEnv(Map<String, Object> env) {
        this.env = env;
    }

    @Override
    public Map<String, Object> getEnv() {
        return env;
    }

    @Override
    public String getHandlerParams() {
        return handlerParams;
    }

    @Override
    public void setHandlerParams(String handlerParams) {
        this.handlerParams = handlerParams;
    }


    @Override
    public void initEnv() {
        if (getEnv().get(initSqlCode) != null) {
            String sqlCode = getEnv().get(initSqlCode).toString();
            sqlCode = sqlCode.replaceAll("\\s*", "");
            if (sqlCode.length() > 0) {
                DatabaseSqlProvider provider = new DatabaseSqlProvider();
                provider.setSqlCode(sqlCode);
                try {
                    List<SqlObject> sqlObjects = provider.provide(null);
                    if (sqlObjects.size() > 0) {
                        SqlObject sqlObject = sqlObjects.get(0);
                        StandardSqlExecutor executor = new StandardSqlExecutor();
                        Result ret = executor.execute(getEnv(), sqlObject);
                        if (ret != null && ret.getSelectResult().size() > 0) {
                            getEnv().putAll(ret.getSelectResult().get(0));
                        }
                    }
                } catch (NoSqlExistsException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public List<SqlObject> initSqlList(List<SqlObject> list) {
        return list;
    }


    /**
     * 每次执行sql前必然执行的，设置环境变量
     *
     * @param sqlObject sql参数
     */
    @Override
    public void setEnvBeforeExecuteEachSql(SqlObject sqlObject) {

    }

    /**
     * 每次执行sql后必然执行的
     *
     * @param sqlObject sql参数
     * @param result    本次执行的结果
     */
    @Override
    public void setEnvAfterExecuteEachSql(SqlObject sqlObject, Result result) {

    }


    @Override
    public Object getCacheResult() {
        return null;
    }

    /**
     * 对最终执行的sql进行处理
     *
     * @param standardQueryObject 初始的查询参数
     * @param sqlObjects          sql列表
     * @param resultMap           对象列表
     * @return 最终的返回结果
     */
    @Override
    public Object setFinalResult(StandardQueryObject standardQueryObject, List<SqlObject> sqlObjects, Map<String, Result> resultMap) {
        Object percentFields = getEnv().get("percentFields");
        Set<String> percetField = new HashSet<>();
        if (percentFields != null) {
            log.info("percentFields=" + percentFields);
            for (String field : (percentFields + "").split(",")) {
                if (field != null && field.length() > 0) {
                    percetField.add(field);
                }
            }
        }
        if (percetField.size() > 0) {
            resultMap.entrySet().forEach(entry -> {
                        if (entry.getValue() != null) {
                            List<Map<String, Object>> list = entry.getValue().getSelectResult();
                            if(list!=null){
                                for (int i = 0; i < list.size(); i++) {
                                    Map<String, Object> tmp = list.get(i);
                                    tmp.forEach((k, v) -> {
                                        if (percetField.contains(k)) {
                                            tmp.put(k, getPercent(v + "", 2));
                                        }
                                    });
                                }
                            }
                        }
                    }
            );
        }
        return defaultSetFinalResult(this, standardQueryObject, sqlObjects, resultMap);
    }

    public static String getPercent(String initResultStr, int pointSize) {
        if (initResultStr == null || initResultStr.indexOf("%") > -1) {
            return initResultStr;
        }
        try {
            if (initResultStr != null && initResultStr.length() > 0) {
                Double result = Double.parseDouble(initResultStr);
                NumberFormat nt = NumberFormat.getPercentInstance();
                //设置百分数保留两位小数
                nt.setMinimumFractionDigits(pointSize);
                nt.setRoundingMode(RoundingMode.HALF_UP);
                String resultStr = nt.format(result).toString();
                if (result > 0) {
                    if (resultStr.indexOf("+") < 0) {
                        resultStr = "+" + resultStr;
                    }
                }
                if (result < 0) {
                    if (resultStr.indexOf("-") < 0) {
                        resultStr = "-" + resultStr;
                    }
                }
                return resultStr;
            }
        } catch (Exception e) {
            return initResultStr;
        }
        return initResultStr;
    }


    public static Object defaultSetFinalResult(SqlExecuteHandler handler, StandardQueryObject standardQueryObject, List<SqlObject> sqlObjects, Map<String, Result> resultMap) {
        if (RESULT_TYPE_RAWLIST.equals(standardQueryObject.getResultType())) {
            SqlObject sqlObject = sqlObjects.get(sqlObjects.size() - 1);
            return resultMap.get(sqlObject.getIdentifier()).getSelectResult();
        } else if (RESULT_TYPE_LIST.equals(standardQueryObject.getResultType())) {
            SqlObject sqlObject = sqlObjects.get(sqlObjects.size() - 1);
            Map<String, Object> mp = new HashMap<>();
            mp.put("records", resultMap.get(sqlObject.getIdentifier()).getSelectResult());
            Integer totalSqlResult = resultMap.get(sqlObject.getIdentifier()).getTotalSqlResult();
            if (totalSqlResult != null) {
                mp.put("totalNum", totalSqlResult);
                Map<String, Object> env = handler.getEnv();
                if (env.get("pageSize") != null) {
                    Integer pageSize = Integer.parseInt(env.get("pageSize").toString());
                    mp.put("totalPages", totalSqlResult % pageSize == 0 ? totalSqlResult / pageSize : totalSqlResult / pageSize + 1);
                }
                if (env.get("pageNum") != null) {
                    mp.put("currentPage", Integer.parseInt(env.get("pageNum").toString()));
                }
            }
            return mp;
        } else if (RESULT_TYPE_MULTILIST.equals(standardQueryObject.getResultType())) {
            return resultMap;
        } else if (RESULT_TYPE_MULTISUM_ONE_LIST.equals(standardQueryObject.getResultType())) {
            SqlObject sqlObject = sqlObjects.get(0);
            List<Map<String, Object>> list = resultMap.get(sqlObject.getIdentifier()).getSelectResult();
            Map<String, Object> mp = new HashMap<>();
            mp.put("records", list);
            for (int i = 1; i < sqlObjects.size(); i++) {
                SqlObject sqlObject1 = sqlObjects.get(i);
                List<Map<String, Object>> list1 = resultMap.get(sqlObject1.getIdentifier()).getSelectResult();
                if (list1 != null) {
                    list1.forEach(tmp -> {
                        if (tmp != null) {
                            mp.putAll(tmp);
                        }
                    });
                }
            }
            return mp;
        } else if (MULTILIST_AND_ONELIST.equals(standardQueryObject.getResultType())) {
            Map<String, Object> mp = new HashMap<>();
            for (int i = 0; i < sqlObjects.size(); i++) {
                SqlObject sqlObject1 = sqlObjects.get(i);
                Result result = resultMap.get(sqlObject1.getIdentifier());
                underlineToCamel(result);
                if (i == 0) {
                    mp.put("records", result.getSelectResult());
                } else {
                    List<Map<String, Object>> selectResult = result.getSelectResult();
                    //这里仅仅处理1个map，一个key的情况
                    if (selectResult != null && selectResult.size() == 1 && selectResult.get(0).size() == 1) {
                        mp.putAll(selectResult.get(0));
                    } else {
                        mp.put(result.getIdentifier(), result.getSelectResult());
                    }
                }
            }
            return mp;
        } else {
            return resultMap;
        }
    }

    /**
     * 下划线转驼峰
     *
     * @param result
     */
    public static void underlineToCamel(Result result) {
        List<Map<String, Object>> selectResult = result.getSelectResult();
        if (selectResult != null) {
            List<Map<String, Object>> ret = new ArrayList<>();
            //下环线转换成驼峰
            selectResult.forEach(e -> {
                if (e != null) {
                    Map<String, Object> mp = new HashMap<>();
                    Set<Map.Entry<String, Object>> entries = e.entrySet();
                    entries.forEach(m -> {
                        String key = m.getKey();
                        Object value = m.getValue();
                        mp.put(StringCamelUtil.underlineToCamel(key), value);
                    });
                    ret.add(mp);
                }
            });
            result.setSelectResult(ret);
        }
    }

    public static Pattern longPattern = Pattern.compile("^\\d{1,}$");
    public static Pattern doublePattern = Pattern.compile("^(\\-|\\+)?\\d+(\\.\\d+)?$");

    public static void parseDataType(Result result) {
        List<Map<String, Object>> list = result.getSelectResult();
        if (list != null && list.size() > 0) {
            list.forEach(map -> {
                try {
                    map.entrySet().forEach(entry -> {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        if (value != null && value instanceof String) {
                            String sv = (String) value;
                            Matcher doubleMatcher = doublePattern.matcher(sv);
                            Matcher longMatcher = longPattern.matcher(sv);
                            if (longMatcher.matches()) {
                                map.put(key, Long.valueOf(sv));
                            } else if (doubleMatcher.matches()) {
                                map.put(key, Double.valueOf(sv));
                            }
                        }
                    });
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            });
        }
    }

    public static Set<String> getHandlerParamsSet(String handlerParams) {
        if (handlerParams == null || handlerParams.trim().length() == 0) {
            return new HashSet<>();
        } else {
            handlerParams = handlerParams.replaceAll("\\s*", "");
            Set<String> set = new HashSet<>();
            Arrays.asList(handlerParams.split(",")).forEach(e -> {
                set.add(e);
            });
            return set;
        }
    }

}
