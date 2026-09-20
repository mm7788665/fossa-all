package com.txy.common;

import lombok.Data;

/**
 * 统一响应结构 —— 对齐前端 data.js 的约定：
 *   { statusCode: 200, message: "ok", result: {...} }
 */
@Data
public class R<T> {

    private int statusCode;
    private String message;
    private T result;

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setStatusCode(200);
        r.setMessage("ok");
        r.setResult(data);
        return r;
    }

    public static <T> R<T> fail(int code, String msg) {
        R<T> r = new R<>();
        r.setStatusCode(code);
        r.setMessage(msg);
        return r;
    }
}
