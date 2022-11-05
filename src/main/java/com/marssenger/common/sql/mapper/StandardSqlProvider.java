package com.marssenger.common.sql.mapper;

import java.util.Map;

/**
 * @author fayfox
 */
public class StandardSqlProvider implements org.apache.ibatis.builder.annotation.ProviderMethodResolver {

    public String select(Map<String, Object> map) {
        return returnScript(map);
    }
    public String insert(Map<String, Object> map) {
        return returnScript(map);
    }
    public String delete(Map<String, Object> map) {
        return returnScript(map);
    }
    public String update(Map<String, Object> map) {
        return returnScript(map);
    }
    public String selectCount(Map<String, Object> map) {
        return returnScript(map);
    }

    private String returnScript(Map<String, Object> map) {
        Map<String, Object> mp=(Map<String, Object>)map.get("params");
        map.putAll(mp);
        String sql=mp.get("sql").toString();
        return "<script>"+sql+"</script>";
    }

}
