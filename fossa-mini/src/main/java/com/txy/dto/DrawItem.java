package com.txy.dto;

/** 单抽结果 */
public class DrawItem {

    private String cardId;
    private String cardName;
    private String rarity;      // SSR / SR / R / N
    private String npcId;
    private Integer isUp;       // 1=UP
    private Integer isPity;     // 1=保底触发
    private Integer drawIndex;  // 本批第几抽
    private Integer isNew;      // 1=首次获得

    public DrawItem() {}

    public static DrawItem of(String cardId, String cardName, String rarity, String npcId,
                              int isUp, int isPity, int drawIndex, int isNew){
        DrawItem d = new DrawItem();
        d.cardId = cardId; d.cardName = cardName; d.rarity = rarity; d.npcId = npcId;
        d.isUp = isUp; d.isPity = isPity; d.drawIndex = drawIndex; d.isNew = isNew;
        return d;
    }

    public String getCardId(){ return cardId; }
    public void setCardId(String cardId){ this.cardId = cardId; }
    public String getCardName(){ return cardName; }
    public void setCardName(String cardName){ this.cardName = cardName; }
    public String getRarity(){ return rarity; }
    public void setRarity(String rarity){ this.rarity = rarity; }
    public String getNpcId(){ return npcId; }
    public void setNpcId(String npcId){ this.npcId = npcId; }
    public Integer getIsUp(){ return isUp; }
    public void setIsUp(Integer isUp){ this.isUp = isUp; }
    public Integer getIsPity(){ return isPity; }
    public void setIsPity(Integer isPity){ this.isPity = isPity; }
    public Integer getDrawIndex(){ return drawIndex; }
    public void setDrawIndex(Integer drawIndex){ this.drawIndex = drawIndex; }
    public Integer getIsNew(){ return isNew; }
    public void setIsNew(Integer isNew){ this.isNew = isNew; }
}
