package com.txy.dto;

import lombok.Data;

/**
 * 统一接口响应结构，对齐前端 { code, data, message }
 */
@Data
public class R<T> {

    private int code;
    private T data;
    private String message;

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(0);
        r.setData(data);
        return r;
    }

    public static <T> R<T> fail(int code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
