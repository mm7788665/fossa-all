package com.txy.fossa.web.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页结果 —— 对齐前端 data.js 约定的 { list, total, pageNum, pageSize }
 */
@Data
public class PageData<T> {
    private List<T> list;
    private long total;
    private int pageNum;
    private int pageSize;

    public static <T> PageData<T> of(List<T> list, long total, int pageNum, int pageSize) {
        PageData<T> p = new PageData<>();
        p.setList(list);
        p.setTotal(total);
        p.setPageNum(pageNum);
        p.setPageSize(pageSize);
        return p;
    }
}
