package com.txy.dto;

/** 设置用户保底规则：PUT /fossa/mini/user/{userId}/pity */
public class UserPityRequest {

    private String poolId;
    private Integer ssrPity;     // 个人 SSR 保底，留空/0 = 跟随奖池
    private Integer srPity;      // 个人 SR 保底
    private Integer minSsrDraws; // 至少抽够 N 抽才可能出 SSR，0=不限
    private String remark;

    public UserPityRequest() {}

    public String getPoolId(){ return poolId; }
    public void setPoolId(String poolId){ this.poolId = poolId; }
    public Integer getSsrPity(){ return ssrPity; }
    public void setSsrPity(Integer ssrPity){ this.ssrPity = ssrPity; }
    public Integer getSrPity(){ return srPity; }
    public void setSrPity(Integer srPity){ this.srPity = srPity; }
    public Integer getMinSsrDraws(){ return minSsrDraws; }
    public void setMinSsrDraws(Integer minSsrDraws){ this.minSsrDraws = minSsrDraws; }
    public String getRemark(){ return remark; }
    public void setRemark(String remark){ this.remark = remark; }
}
