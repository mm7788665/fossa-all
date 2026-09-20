package com.txy.entity;

import java.time.LocalDateTime;

/** 用户保底进度：每个用户每个奖池一条 */
public class GachaUserPity {

    private String id;
    private String userId;
    private String poolId;
    private Integer ssrCount;    // 距上次出 SSR 已抽数
    private Integer srCount;     // 距上次出 SR 已抽数
    private Integer totalCount;  // 累计抽数
    private Integer ssrTotal;    // 累计出 SSR 数

    /** 用户级 SSR 保底覆盖：NULL = 跟随奖池 pity_ssr */
    private Integer pitySsrOverride;
    /** 用户级 SR 保底覆盖：NULL = 跟随奖池 pity_sr */
    private Integer pitySrOverride;
    /** 最低门槛：累计抽数 < 该值时绝对不出 SSR（0 = 不限制） */
    private Integer minSsrDraws;
    /** 用户级 SSR 窗口起点覆盖：NULL = 跟随奖池 */
    private Integer pitySsrStartOverride;
    /** 备注 */
    private String remark;

    private LocalDateTime updatedAt;

    public GachaUserPity() {}

    /** 新用户初始化 */
    public static GachaUserPity init(String id, String userId, String poolId){
        GachaUserPity p = new GachaUserPity();
        p.id = id; p.userId = userId; p.poolId = poolId;
        p.ssrCount = 0; p.srCount = 0; p.totalCount = 0; p.ssrTotal = 0;
        p.pitySsrOverride = null; p.pitySrOverride = null; p.minSsrDraws = 0; p.remark = "";
        return p;
    }

    public String getId(){ return id; }
    public void setId(String id){ this.id = id; }
    public String getUserId(){ return userId; }
    public void setUserId(String userId){ this.userId = userId; }
    public String getPoolId(){ return poolId; }
    public void setPoolId(String poolId){ this.poolId = poolId; }
    public Integer getSsrCount(){ return ssrCount == null ? 0 : ssrCount; }
    public void setSsrCount(Integer ssrCount){ this.ssrCount = ssrCount; }
    public Integer getSrCount(){ return srCount == null ? 0 : srCount; }
    public void setSrCount(Integer srCount){ this.srCount = srCount; }
    public Integer getTotalCount(){ return totalCount == null ? 0 : totalCount; }
    public void setTotalCount(Integer totalCount){ this.totalCount = totalCount; }
    public Integer getSsrTotal(){ return ssrTotal == null ? 0 : ssrTotal; }
    public void setSsrTotal(Integer ssrTotal){ this.ssrTotal = ssrTotal; }
    public Integer getPitySsrOverride(){ return pitySsrOverride; }
    public void setPitySsrOverride(Integer v){ this.pitySsrOverride = v; }
    public Integer getPitySrOverride(){ return pitySrOverride; }
    public void setPitySrOverride(Integer v){ this.pitySrOverride = v; }
    public Integer getMinSsrDraws(){ return minSsrDraws == null ? 0 : minSsrDraws; }
    public void setMinSsrDraws(Integer v){ this.minSsrDraws = v; }
    public Integer getPitySsrStartOverride(){ return pitySsrStartOverride; }
    public void setPitySsrStartOverride(Integer v){ this.pitySsrStartOverride = v; }
    public String getRemark(){ return remark; }
    public void setRemark(String remark){ this.remark = remark; }

    /** 生效的 SSR 保底：用户覆盖优先，否则跟随奖池 */
    public int effectiveSsrPity(int poolPity){
        return (pitySsrOverride != null && pitySsrOverride > 0) ? pitySsrOverride : poolPity;
    }
    /** 生效的 SSR 出货窗口起点：用户覆盖优先，否则跟随奖池（且不超过生效保底） */
    public int effectiveSsrStart(int poolStart){
        int v = (pitySsrStartOverride != null && pitySsrStartOverride >= 0) ? pitySsrStartOverride : poolStart;
        return v < 0 ? 0 : v;
    }
    /** 生效的 SR 保底 */
    public int effectiveSrPity(int poolPity){
        return (pitySrOverride != null && pitySrOverride > 0) ? pitySrOverride : poolPity;
    }

    public LocalDateTime getUpdatedAt(){ return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt){ this.updatedAt = updatedAt; }
}
