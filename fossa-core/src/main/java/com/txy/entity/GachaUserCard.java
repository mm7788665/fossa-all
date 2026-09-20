package com.txy.entity;

import java.time.LocalDateTime;

/** 用户卡牌：去重持有，重复获得累加 count */
public class GachaUserCard {

    private String id;
    private String userId;
    private String cardId;
    private String poolId;
    private String cardName;
    private String rarity;
    private Integer count;
    private LocalDateTime firstAt;
    private LocalDateTime updatedAt;

    public GachaUserCard() {}

    public static GachaUserCard of(String id, String userId, String cardId, String poolId,
                                   String cardName, String rarity){
        GachaUserCard c = new GachaUserCard();
        c.id = id; c.userId = userId; c.cardId = cardId; c.poolId = poolId;
        c.cardName = cardName; c.rarity = rarity; c.count = 1;
        return c;
    }

    public String getId(){ return id; }
    public void setId(String id){ this.id = id; }
    public String getUserId(){ return userId; }
    public void setUserId(String userId){ this.userId = userId; }
    public String getCardId(){ return cardId; }
    public void setCardId(String cardId){ this.cardId = cardId; }
    public String getPoolId(){ return poolId; }
    public void setPoolId(String poolId){ this.poolId = poolId; }
    public String getCardName(){ return cardName; }
    public void setCardName(String cardName){ this.cardName = cardName; }
    public String getRarity(){ return rarity; }
    public void setRarity(String rarity){ this.rarity = rarity; }
    public Integer getCount(){ return count == null ? 0 : count; }
    public void setCount(Integer count){ this.count = count; }
    public LocalDateTime getFirstAt(){ return firstAt; }
    public void setFirstAt(LocalDateTime firstAt){ this.firstAt = firstAt; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt){ this.updatedAt = updatedAt; }
}
