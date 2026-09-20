package com.txy.dto;


public enum StatusCode {

    /**
     * 请求成功
     */
    OK(200, "请求成功"),
    /**
     * 请求失败，例如：请求参数有误
     */
    BAD_REQUEST(301, "请求参数有误"),
    /**
     * 找不到记录
     */
    NOT_FOUND(302, "找不到记录"),
    /**
     * 找不到记录
     */
    NOT_MEMBER(303, "非会员"),
    /**
     * 页面:没有导购配置
     */
    NOT_GUIDE(304, "没有导购配置"),

    BJXFJ_FAIL(305,"实名认证后可以参与活动"),
    /**
     * 请求失败
     */
    ERROR(400, "请求失败"),
    /**
     * 用户授权Session超时
     */
    SESSION_TIMEOUT(401, "会话过期"),
    /**
     * 请求RPC接口超时
     */
    DUBBO_TIMEOUT(402, "请求RPC接口超时"),
    /**
     * 门店不一致，请求门店与用户授权归属门店不同
     */
    STORE_DIFFERENT(403, "门店不一致"),

    /**
     * 客户信息不一致
     */
    USER_DIFFERENT(405, "回收优惠活动已被其他人领取"),
    /**
     * 客户信息不一致
     */
    JDX_ERR(406, "没有找到相关的回收订单"),
    /**
     * 积分不足
     */
    LACK_INTEGRAL(102,"积分不足"),


    /**
     * 签名不存在
     */
    CHECK_SIGN_1(501,"签名不存在"),


    /**
     * 签名错误
     */
    CHECK_SIGN_2(502,"签名错误"),
    /**
     * 保单不存在
     */
    CHECK_SIGN_3(503,"保单不存在"),
    /**
     * 保单不存在
     */
    CHECK_SIGN_4(504,"该保单已退回积分，暂不允许继续操作");

    private int value;
    private String reasonPhrase;

    StatusCode(int value, String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    public int getValue() {
        return value;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public static StatusCode findBy(int statusCode) {
        for (StatusCode status : values()) {
            if (status.value == statusCode) {
                return status;
            }
        }
        throw new IllegalArgumentException("No matching constant for [" + statusCode + "]");
    }
}
