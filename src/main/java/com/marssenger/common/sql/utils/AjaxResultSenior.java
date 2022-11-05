package com.marssenger.common.sql.utils;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

public class AjaxResultSenior extends AjaxResult {

    public static final String DATA_UPDATE_TAG = "dataUpdateTime";

    /**
     * 初始化一个新创建的 AjaxResult 对象，使其表示一个空消息。
     */
    public AjaxResultSenior() {

    }

    /**
     * 初始化一个新创建的 AjaxResult 对象
     *
     * @param code 状态码
     * @param msg  返回内容
     * @param data 数据对象
     */
    public AjaxResultSenior(int code, String msg, Object data, Date time) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (data != null) {
            super.put(DATA_TAG, data);
        }

//        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
        String formatDate = null;
        if (!Objects.isNull(time)) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
            Instant date = time.toInstant();
            LocalDateTime localDateTime = LocalDateTime.ofInstant(date, ZoneId.of("GMT+08:00"));
            formatDate = df.format(localDateTime);
        }
        super.put(DATA_UPDATE_TAG, formatDate);
    }

    /**
     * 返回成功数据
     *
     * @param t
     * @return 成功消息
     */
    public static AjaxResultSenior success(Object t) {
        return new AjaxResultSenior(HttpStatus.OK.value(), null, t, null);
    }

    /**
     * 返回成功数据
     *
     * @return 成功消息
     */
    public static AjaxResultSenior success(Object data, Date time) {
        return new AjaxResultSenior(HttpStatus.OK.value(), null, data, time);
    }


    /**
     * 返回成功消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 成功消息
     */
    public static AjaxResultSenior success(String msg, Object data, Date time) {
        return new AjaxResultSenior(HttpStatus.OK.value(), msg, data, time);
    }

    /**
     * 返回错误消息
     *
     * @param msg  返回内容
     * @param data 数据对象
     * @return 警告消息
     */
    public static AjaxResultSenior error(String msg, Object data, Date time) {
        return new AjaxResultSenior(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg, data, time);
    }


}
