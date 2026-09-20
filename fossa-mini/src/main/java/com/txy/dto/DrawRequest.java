package com.txy.dto;

/** 抽卡请求：POST /fossa/mini/draw */
public class DrawRequest {

    private String userId;   // 用户ID（小程序 openid 或业务 uid）
    private String poolId;   // 奖池ID
    private Integer times;   // 1=单抽，10=十连

    public DrawRequest() {}

    public String getUserId(){ return userId; }
    public void setUserId(String userId){ this.userId = userId; }
    public String getPoolId(){ return poolId; }
    public void setPoolId(String poolId){ this.poolId = poolId; }
    public Integer getTimes(){ return times; }
    public void setTimes(Integer times){ this.times = times; }
}
