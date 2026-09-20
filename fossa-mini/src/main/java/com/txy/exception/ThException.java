package com.txy.exception;

/**
 * Created by rocky on 15/3/20.
 */
public class ThException extends RuntimeException {

    public ThException() {
    }

    public ThException(String message) {
        super(message);
    }

    public ThException(String errCode, String errMsg) {
        super("errCode:"+errCode+" , errMsg:"+errMsg);
    }

    public ThException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThException(Throwable cause) {
        super(cause);
    }

    public ThException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
