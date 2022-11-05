package com.marssenger.common.sql.domain;

/**
 * @author xieyoujun
 * @version 1.0
 * @date 2021/11/2917:47
 **/

import lombok.Data;

@Data
public class PageVo {
    //当前页
    private int currentPage;

    //页数
    private int totalPages;

    //总条数
    private long totalNum;

    private Object records;

    private Object total;

    private String showVal;
    
}
