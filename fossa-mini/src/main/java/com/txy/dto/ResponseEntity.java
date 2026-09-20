package com.txy.dto;



import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
}
