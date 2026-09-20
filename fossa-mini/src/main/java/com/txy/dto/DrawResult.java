package com.txy.dto;

import java.util.List;

/** 抽卡结果：POST /fossa/mini/draw 的返回体 */
public class DrawResult {

    private String batchNo;
    private String poolId;
    private String poolName;
    private List<DrawItem> items;    // 本次抽到的卡（按抽的顺序）
    private Integer cost;            // 本次消耗
    /* 抽完之后的保底进度，前端可直接展示"再抽 N 抽必出 SSR" */
    private PityInfo pity;

    public DrawResult() {}

    public String getBatchNo(){ return batchNo; }
    public void setBatchNo(String batchNo){ this.batchNo = batchNo; }
    public String getPoolId(){ return poolId; }
    public void setPoolId(String poolId){ this.poolId = poolId; }
    public String getPoolName(){ return poolName; }
    public void setPoolName(String poolName){ this.poolName = poolName; }
    public List<DrawItem> getItems(){ return items; }
    public void setItems(List<DrawItem> items){ this.items = items; }
    public Integer getCost(){ return cost; }
    public void setCost(Integer cost){ this.cost = cost; }
    public PityInfo getPity(){ return pity; }
    public void setPity(PityInfo pity){ this.pity = pity; }

    /** 保底进度快照 */
    public static class PityInfo {
        private Integer ssrCount;      // 已累计未出 SSR 的抽数
        private Integer ssrPity;       // SSR 保底抽数
        private Integer ssrRemain;     // 再抽几抽必出 SSR（无保底为 -1）
        private Integer srCount;
        private Integer srPity;
        private Integer srRemain;
        private Integer totalCount;    // 累计抽数

        /* ---- 用户级规则（本次新增） ---- */
        private Integer minSsrDraws;   // 最低门槛：累计抽够 N 抽才可能出 SSR，0=不限
        private Integer unlockRemain;  // 还需再抽几抽才解锁 SSR
        private Integer locked;        // 1=当前处于锁定状态（未达门槛）
        private Integer personal;      // 1=该用户用了个人保底（与奖池默认不同）

        /* ---- 出货窗口 ---- */
        private Integer ssrStart;        // SSR 窗口起点：满 N 抽后才可能出
        private Integer ssrWindowRemain; // 还需再抽几抽才进窗口（0=已进入）

        public PityInfo() {}

        public static PityInfo of(int ssrCount, int ssrPity, int srCount, int srPity, int totalCount){
            PityInfo p = new PityInfo();
            p.ssrCount = ssrCount; p.ssrPity = ssrPity;
            p.ssrRemain = ssrPity > 0 ? Math.max(0, ssrPity - ssrCount) : -1;
            p.srCount = srCount; p.srPity = srPity;
            p.srRemain = srPity > 0 ? Math.max(0, srPity - srCount) : -1;
            p.totalCount = totalCount;
            return p;
        }

        public Integer getSsrCount(){ return ssrCount; }
        public void setSsrCount(Integer v){ this.ssrCount = v; }
        public Integer getSsrPity(){ return ssrPity; }
        public void setSsrPity(Integer v){ this.ssrPity = v; }
        public Integer getSsrRemain(){ return ssrRemain; }
        public void setSsrRemain(Integer v){ this.ssrRemain = v; }
        public Integer getSrCount(){ return srCount; }
        public void setSrCount(Integer v){ this.srCount = v; }
        public Integer getSrPity(){ return srPity; }
        public void setSrPity(Integer v){ this.srPity = v; }
        public Integer getSrRemain(){ return srRemain; }
        public void setSrRemain(Integer v){ this.srRemain = v; }
        public Integer getTotalCount(){ return totalCount; }
        public void setTotalCount(Integer v){ this.totalCount = v; }
        public Integer getMinSsrDraws(){ return minSsrDraws; }
        public void setMinSsrDraws(Integer v){ this.minSsrDraws = v; }
        public Integer getUnlockRemain(){ return unlockRemain; }
        public void setUnlockRemain(Integer v){ this.unlockRemain = v; }
        public Integer getLocked(){ return locked; }
        public void setLocked(Integer v){ this.locked = v; }
        public Integer getPersonal(){ return personal; }
        public void setPersonal(Integer v){ this.personal = v; }
        public Integer getSsrStart(){ return ssrStart; }
        public void setSsrStart(Integer v){ this.ssrStart = v; }
        public Integer getSsrWindowRemain(){ return ssrWindowRemain; }
        public void setSsrWindowRemain(Integer v){ this.ssrWindowRemain = v; }
    }
}
