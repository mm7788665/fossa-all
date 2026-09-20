package com.txy.controller.respon;


import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 类ResponseEntity的实现描述：返回结果信息
 *
 * @author wuzh@hyxt.com 2017/7/13 10:12
 * @since JDK 1.7
 */
@Data
public class ResponseEntity<T> implements Serializable {
    private static final long serialVersionUID = 3272784654055909262L;

    /**
     * 结果编号
     * 200=请求成功
     * 301=表示传入额参数有问题
     * 302=根据参数找不到数据
     * 400=请求失败，例如处理出现异常
     * 401=用户授权Session会话过期
     * 402=请求RPC接口超时
     */
    private Integer statusCode;
    /**
     * 返回结果消息
     */
    private String message;
    /**
     * 返回结果数据
     */
    private T result;
    /**
     * 错误信息
     */
    private List<Error> errors;


    public ResponseEntity() {
    }

    public ResponseEntity(Integer statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public ResponseEntity(Integer statusCode, String message, T result) {
        this.statusCode = statusCode;
        this.message = message;
        this.result = result;
    }

    public Integer getCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }


}
