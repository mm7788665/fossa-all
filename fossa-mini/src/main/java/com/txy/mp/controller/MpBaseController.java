package com.txy.mp.controller;

import javax.servlet.http.HttpServletRequest;

/**
 * 小程序接口基类
 *  - playerId 由 MpAuthInterceptor 依据 header sessionId 解析后写入 request
 *  - 未登录时为 null（游客可读接口照常返回，需要登录的接口自行 401）
 */
public abstract class MpBaseController {

    protected String playerId(HttpServletRequest request) {
        Object v = request.getAttribute("mpPlayerId");
        return v == null ? null : String.valueOf(v);
    }

    protected String requirePlayerId(HttpServletRequest request) {
        String pid = playerId(request);
        if (pid == null || pid.isEmpty()) {
            throw new MpAuthException();
        }
        return pid;
    }

    /** 未登录异常，由 GlobalExceptionHandler 转 401 */
    public static class MpAuthException extends RuntimeException {
        public MpAuthException() {
            super("请先登录");
        }
    }
}
