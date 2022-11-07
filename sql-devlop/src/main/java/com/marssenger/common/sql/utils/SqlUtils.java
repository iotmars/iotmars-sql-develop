package com.marssenger.common.sql.utils;

import java.io.LineNumberReader;
import java.io.StringReader;

/**
 * 用来格式化sql
 *
 * @author ligaosheng
 */
public class SqlUtils {

    public static final String ONE_SPACE = " ";
    public static final String CDATA1 = "<![CDATA[";
    public static final String CDATA2 = "]]>";

    public static String formatToOneLine(String inputSql) {
        if (inputSql == null) {
            return null;
        }
        inputSql=inputSql.replaceAll("\\s*-- .*\n", ONE_SPACE).replaceAll("\\s*", ONE_SPACE);
        return inputSql;
    }

    public static String getPerfectSql(String inputSql) {
        try {
            //先去空行
            inputSql=inputSql.replaceAll("^\n+|\n+$","");
            //再去空格
            inputSql=inputSql.trim();
            if(inputSql.indexOf(CDATA1)==0){
                inputSql=inputSql.substring(CDATA1.length());
                inputSql=inputSql.substring(0,inputSql.length()-CDATA2.length());
            }
            //先去空行
            inputSql=inputSql.replaceAll("^\n+|\n+$","");
            //再去空格
            inputSql=inputSql.trim();
            for (int i = 0; i <100; i++) {
                if(inputSql.indexOf("\n")==0){
                    inputSql=inputSql.substring("\n".length());
                }else{
                    break;
                }
            }
            StringBuffer stringBuffer = new StringBuffer();
            LineNumberReader lnr = new LineNumberReader(new StringReader(inputSql));
            String line;
            while ((line = lnr.readLine()) != null) {
                if(line.trim().equals("<![CDATA[")||line.trim().equals("]]>")){
                    continue;
                }
                stringBuffer.append("/**" + ONE_SPACE + lnr.getLineNumber() + ONE_SPACE + "*/" + ONE_SPACE + ONE_SPACE + ONE_SPACE + ONE_SPACE + line + "\n");
            }
            lnr.close();
            return stringBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
