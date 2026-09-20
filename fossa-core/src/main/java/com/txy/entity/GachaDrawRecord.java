package com.txy.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/** 抽卡记录：每一抽一条 */
public class GachaDrawRecord {

    private String id;
    private String batchNo;      // 批次号，十连同一批
    private String userId;
    private String poolId;
    private String cardId;
    private String cardName;
    private String rarity;
    private Integer isUp;        // 1=UP
    private Integer drawIndex;   // 本批第几抽 1-10
    private Integer isPity;      // 1=保底触发
    private Integer cost;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public GachaDrawRecord() {}

    public static GachaDrawRecord of(String id, String batchNo, String userId, String poolId,
                                     String cardId, String cardName, String rarity,
                                     int isUp, int drawIndex, int isPity, int cost){
        GachaDrawRecord r = new GachaDrawRecord();
        r.id = id; r.batchNo = batchNo; r.userId = userId; r.poolId = poolId;
        r.cardId = cardId; r.cardName = cardName; r.rarity = rarity;
        r.isUp = isUp; r.drawIndex = drawIndex; r.isPity = isPity; r.cost = cost;
        return r;
    }

    public String getId(){ return id; }
    public void setId(String id){ this.id = id; }
    public String getBatchNo(){ return batchNo; }
    public void setBatchNo(String batchNo){ this.batchNo = batchNo; }
    public String getUserId(){ return userId; }
    public void setUserId(String userId){ this.userId = userId; }
    public String getPoolId(){ return poolId; }
    public void setPoolId(String poolId){ this.poolId = poolId; }
    public String getCardId(){ return cardId; }
    public void setCardId(String cardId){ this.cardId = cardId; }
    public String getCardName(){ return cardName; }
    public void setCardName(String cardName){ this.cardName = cardName; }
    public String getRarity(){ return rarity; }
    public void setRarity(String rarity){ this.rarity = rarity; }
    public Integer getIsUp(){ return isUp; }
    public void setIsUp(Integer isUp){ this.isUp = isUp; }
    public Integer getDrawIndex(){ return drawIndex; }
    public void setDrawIndex(Integer drawIndex){ this.drawIndex = drawIndex; }
    public Integer getIsPity(){ return isPity; }
    public void setIsPity(Integer isPity){ this.isPity = isPity; }
    public Integer getCost(){ return cost; }
    public void setCost(Integer cost){ this.cost = cost; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
}
