package com.marssenger.common.sql.executor;

import com.marssenger.common.sql.domain.CommandObject;
import com.marssenger.common.sql.domain.SqlObject;
import com.marssenger.common.sql.domain.Result;
import com.marssenger.common.sql.handler.SqlExecuteHandler;
import com.marssenger.common.sql.mapper.StandardSqlMapper;
import com.marssenger.common.sql.utils.SpringUtilsForSql;
import com.marssenger.common.sql.utils.SqlUtils;
import com.marssenger.common.sql.utils.StringCamelUtil;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 标准的SQL执行器
 *
 * @author ligaosheng
 */
@Slf4j
public class StandardSqlExecutor implements SqlExecutor {


    public static String spilter = "========================================================================================================================================================================";

    public final static String SORT = "sort";
    public final static String SORT_FIELD = "sortField";
    public final static String SQL = "sql";
    public final static String INSERT = "insert";
    public final static String DELETE = "delete";
    public final static String UPDATE = "update";
    public final static String SELECT = "select";

    private SqlExecuteHandler sqlExecuteHandler;

    private String executorParams;

    @Override
    public void init() {

    }

    @Override
    public void setSqlExecuteHandler(SqlExecuteHandler sqlExecuteHandler) {
        this.sqlExecuteHandler = sqlExecuteHandler;
    }

    @Override
    public SqlExecuteHandler getSqlExecuteHandler() {
        return sqlExecuteHandler;
    }

    @Override
    public String getExecutorParams() {
        return executorParams;
    }

    @Override
    public void setExecutorParams(String executorParams) {
        this.executorParams = executorParams;
    }

    @Override
    public Result execute(Map<String, Object> lastEnvMap, SqlObject sqlObject) {
        if (CommandObject.COMMON_COMMAND.equals(sqlObject.getCommandType())) {
            Object commandResult = doCommand(sqlObject);
            Result result = new Result();
            result.setCommandResult(commandResult);
            return result;
        }
        return executeOneSql(lastEnvMap, sqlObject);
    }

    @Override
    public Object doCommand(CommandObject commandObject) {
        return null;
    }


    public static Result executeOneSql(final Map<String, Object> envMap, SqlObject sqlObject) {

        Map<String, Object> lastEnvMap = new HashMap<>();
        lastEnvMap.putAll(envMap);

        StandardSqlMapper cardListMapper = SpringUtilsForSql.getBean(StandardSqlMapper.class);
        checkNull("sqlType", sqlObject.getSqlType());
        checkNull("sql", sqlObject.getSql());
        log.info("sqlType=" + sqlObject.getSqlType() + ",sqlCode=" + sqlObject.getSqlCode() + ",sql=\n" + spilter + "\n\n" + SqlUtils.getPerfectSql(sqlObject.getSql()) + "\n" + spilter);
        String sqlType = sqlObject.getSqlType().toLowerCase();
        String sql = sqlObject.getSql().trim();
        lastEnvMap.put(SQL, sql);
        if (lastEnvMap.get(SORT) != null) {
            Object sortFieldObj = lastEnvMap.get(SORT_FIELD);
            if (sortFieldObj != null) {
                String sortField = sortFieldObj.toString();
                if (sortField != null && sortField.trim().length() > 0) {
                    sortField = StringCamelUtil.camelToUnderline(sortField);
                    lastEnvMap.put(SORT_FIELD, sortField);
                }
            }
        }
        List<Map<String, Object>> selectResult = null;
        Integer selectCountResult = null;
        Integer insertResult = null;
        Integer updateResult = null;
        Integer deleteResult = null;
        lastEnvMap.putAll(sqlObject.getJsonMap().getInnerMap());
        logMap(lastEnvMap);
        if (sqlObject.getSqlType() == null || SELECT.equals(sqlType)) {
            selectResult = cardListMapper.select(lastEnvMap);
        } else {
            if (INSERT.equals(sqlType)) {
                insertResult = cardListMapper.insert(lastEnvMap);
            }
            if (UPDATE.equals(sqlType)) {
                updateResult = cardListMapper.update(lastEnvMap);
            }
            if (DELETE.equals(sqlType)) {
                deleteResult = cardListMapper.delete(lastEnvMap);
            }
        }
        String totalSql = sqlObject.getTotalSql();
        if (totalSql != null && totalSql.trim().length() > 0) {
            log.info("totalSql=\n" + spilter + "\n" + SqlUtils.getPerfectSql(totalSql) + "\n" + spilter);
            logMap(lastEnvMap);
            lastEnvMap.put(SQL, totalSql);
            selectCountResult = cardListMapper.selectCount(lastEnvMap);
        }
        Result result = new Result();
        result.setInsertResult(insertResult);
        result.setDeleteResult(deleteResult);
        result.setUpdateResult(updateResult);
        result.setSelectResult(selectResult);
        result.setSqlResult(chooseOneNotNull(insertResult, deleteResult, updateResult, selectResult));
        result.setTotalSqlResult(selectCountResult);
        result.setIdentifier(sqlObject.getIdentifier());
        return result;
    }


    public static void checkNull(String name, Object obj) {
        if (obj == null || ((obj instanceof String) && ((String) obj).trim().length() == 0)) {
            throw new NullPointerException(name + "为空!");
        }
    }


    public static boolean isNotNull(Object obj) {
        if (obj == null || ((obj instanceof String) && ((String) obj).trim().length() == 0)) {
            return false;
        }
        return true;
    }

    public static Object chooseOneNotNull(Object... objs) {
        Object ret = null;
        for (int i = 0; i < objs.length; i++) {
            if (isNotNull(objs[i])) {
                ret = objs[i];
                break;
            }
        }
        return ret;
    }

    public static String getHash(Object o) {
        if (o == null) {
            return null;
        } else {
            return o.hashCode() + "";
        }
    }

    public static void logMap(Map<String, Object> map) {
        ArrayList<String[]> arrayList = new ArrayList<>();
        map.entrySet().forEach(e -> {
            arrayList.add(new String[]{e.getKey(), e.getValue() == null ? null : e.getValue().getClass() + "", getHash(e.getValue()), getSummary(e.getValue())});
        });
        arrayList.sort((o1, o2) -> {
            int i1 = o1[0].indexOf(0);
            int i2 = o2[0].indexOf(0);
            return i1 - i2;
        });
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("运行环境:" + "\n");
        stringBuffer.append(spilter + "\n");
        for (int i = 0; i < arrayList.size(); i++) {
            stringBuffer.append(arrayList.get(i)[0] + ":" + arrayList.get(i)[1] + "," + arrayList.get(i)[2] + "," + arrayList.get(i)[3] + "\n");
        }
        stringBuffer.append(spilter);
        log.info(stringBuffer.toString());
    }

    public static String getSummary(Object obj) {

        if (obj == null) {
            return null;
        }

        String defaultVal = "value=" + obj;

        if (obj instanceof String) {
            String raw = (String) obj;
            raw = raw.replace("\\s*", " ");
            if (raw.length() > 150) {
                raw = raw.substring(0, 150) + " ... ";
            }
            return raw;
        }

        if (obj instanceof Byte) {
            return defaultVal;
        }

        if (obj instanceof Short) {
            return defaultVal;
        }

        if (obj instanceof Integer) {
            return defaultVal;
        }

        if (obj instanceof Long) {
            return defaultVal;
        }

        if (obj instanceof Float) {
            return defaultVal;
        }

        if (obj instanceof Double) {
            return defaultVal;
        }

        if (obj instanceof Character) {
            return defaultVal;
        }

        if (obj instanceof Collection) {
            return "collection.size=" + ((Collection) obj).size();
        }

        if (obj instanceof Date) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return "value=" + simpleDateFormat.format((Date) obj);
        }

        return null;
    }
}
