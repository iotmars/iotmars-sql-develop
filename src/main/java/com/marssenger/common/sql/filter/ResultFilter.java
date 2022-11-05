package com.marssenger.common.sql.filter;

import com.marssenger.common.sql.domain.Result;

import java.util.List;

/**
 * 通用代码，对结果的过滤
 *
 * @author ligaosheng
 */
public interface ResultFilter {
    /**
     * 过滤一个结果，然后得到新的结果
     * @param results
     * @return
     */
    public List<Result> filter(List<Result> results);
}
