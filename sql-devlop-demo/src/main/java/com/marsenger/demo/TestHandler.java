package com.marsenger.demo;


import com.marssenger.common.sql.handler.StandardSqlExecuteHandler;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

@Component("testHandler")
public class TestHandler extends StandardSqlExecuteHandler {

    /**
     * 初始化环境，当您的handlerParams包含setDateRange这个参数时，为您设置当前月份的开始时间和当前月份的结束时间，您可以在使用mybatis的#{startDay}、#{endDay}符号调用
     */
    @Override
    public void initEnv() {
        Map<String, Object> env = getEnv();
        String handlerParams = getHandlerParams();
        Set<String> handlerParamsSet = getHandlerParamsSet(handlerParams);
        if (handlerParamsSet.contains("setDateRange")) {
            Calendar calendar = Calendar.getInstance();
            //获取当月第一天
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String yearMonth = sdf.format(new Date());
            env.put("startDay", yearMonth + "-" + "01");
            //获取当月最后一天
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            env.put("endDay", yearMonth + "-" + getDays(year, month));
        }
    }

    /**
     * 获取当前月份有多少天，可能28、29、30、31天
     *
     * @param year
     * @param month
     * @return
     */
    public static int getDays(int year, int month) {
        int days = 0;
        if (month != 2) {
            switch (month) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    days = 31;
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    days = 30;

            }
        } else {
            // 闰年
            if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) {
                days = 29;
            } else {
                days = 28;
            }
        }
        return days;
    }


}
