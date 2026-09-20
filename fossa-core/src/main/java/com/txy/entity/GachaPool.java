package com.txy.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;

/** 抽卡奖池 */
@JsonIgnoreProperties(ignoreUnknown = true)   // 前端会多传一些展示用字段，忽略即可
public class GachaPool {

    private String id;
    private String name;
    private String status;       // 启用 / 未启用
    private String currency;     // 货币名
    private Integer costSingle;
    private Integer costTen;

    private Integer pitySsr;     // SSR 保底抽数，0=不保底
    private Integer pitySr;
    /** SSR 出货窗口起点：距上次出 SSR 满 N 抽后才可能出，0=不限（<= pitySsr） */
    private Integer pitySsrStart;
    /** SR 出货窗口起点 */
    private Integer pitySrStart;
    private Integer resetOnHit;  // 1=出货后重置计数

    private String upNpcId;      // UP 角色
    private java.math.BigDecimal upBonus;

    /** 全局默认最低门槛：累计抽够 N 抽才可能出 SSR，0=不限（玩家级可覆盖） */
    private Integer minSsrDraws;

    private Integer sort;
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 非数据库字段：奖池下的卡牌，列表/详情接口填充 */
    private List<GachaCard> cards;

    public GachaPool() {}

    public String getId(){ return id; }
    public void setId(String id){ this.id = id; }
    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
    public String getStatus(){ return status; }
    public void setStatus(String status){ this.status = status; }
    public String getCurrency(){ return currency; }
    public void setCurrency(String currency){ this.currency = currency; }
    public Integer getCostSingle(){ return costSingle; }
    public void setCostSingle(Integer costSingle){ this.costSingle = costSingle; }
    public Integer getCostTen(){ return costTen; }
    public void setCostTen(Integer costTen){ this.costTen = costTen; }
    public Integer getPitySsr(){ return pitySsr == null ? 0 : pitySsr; }
    public void setPitySsr(Integer pitySsr){ this.pitySsr = pitySsr; }
    public Integer getPitySr(){ return pitySr == null ? 0 : pitySr; }
    public void setPitySr(Integer pitySr){ this.pitySr = pitySr; }
    public Integer getPitySsrStart(){
        int v = pitySsrStart == null ? 0 : pitySsrStart;
        int k = getPitySsr();
        return (k > 0 && v > k) ? k : v;      // 起点不能超过保底，否则永远出不了
    }
    public void setPitySsrStart(Integer v){ this.pitySsrStart = v; }
    public Integer getPitySrStart(){
        int v = pitySrStart == null ? 0 : pitySrStart;
        int k = getPitySr();
        return (k > 0 && v > k) ? k : v;
    }
    public void setPitySrStart(Integer v){ this.pitySrStart = v; }
    public Integer getResetOnHit(){ return resetOnHit == null ? 1 : resetOnHit; }
    public void setResetOnHit(Integer resetOnHit){ this.resetOnHit = resetOnHit; }
    public String getUpNpcId(){ return upNpcId; }
    public void setUpNpcId(String upNpcId){ this.upNpcId = upNpcId; }
    public java.math.BigDecimal getUpBonus(){ return upBonus; }
    public void setUpBonus(java.math.BigDecimal upBonus){ this.upBonus = upBonus; }
    public Integer getMinSsrDraws(){ return minSsrDraws == null ? 0 : minSsrDraws; }
    public void setMinSsrDraws(Integer minSsrDraws){ this.minSsrDraws = minSsrDraws; }
    public Integer getSort(){ return sort == null ? 0 : sort; }
    public void setSort(Integer sort){ this.sort = sort; }
    public Integer getDeleted(){ return deleted; }
    public void setDeleted(Integer deleted){ this.deleted = deleted; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt){ this.updatedAt = updatedAt; }
    public List<GachaCard> getCards(){ return cards; }
    public void setCards(List<GachaCard> cards){ this.cards = cards; }
}
