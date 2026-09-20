package com.txy.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

/** 奖池卡牌 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GachaCard {

    private String id;
    private String poolId;
    private String name;
    /** 缩略图地址：列表 / 抽卡结果 / 十连翻牌 */
    private String thumb;
    /** 大图地址：奖品详情 / 全屏立绘 / 分享图 */
    private String image;
    private String npcId;      // 关联 NPC
    private String rarity;     // SSR / SR / R / N
    private Integer weight;    // 同稀有度内相对权重
    private Integer stock;     // -1=不限量，0=不出，>0 出一次减一
    private Integer isUp;      // 1=概率 UP
    private Integer sort;
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public GachaCard() {}

    public String getId(){ return id; }
    public void setId(String id){ this.id = id; }
    public String getPoolId(){ return poolId; }
    public void setPoolId(String poolId){ this.poolId = poolId; }
    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }
    public String getThumb(){ return thumb == null ? "" : thumb; }
    public void setThumb(String thumb){ this.thumb = thumb; }
    public String getImage(){ return image == null ? "" : image; }
    public void setImage(String image){ this.image = image; }
    public String getNpcId(){ return npcId; }
    public void setNpcId(String npcId){ this.npcId = npcId; }
    public String getRarity(){ return rarity; }
    public void setRarity(String rarity){ this.rarity = rarity; }
    public Integer getWeight(){ return weight == null ? 0 : weight; }
    public void setWeight(Integer weight){ this.weight = weight; }
    public Integer getStock(){ return stock == null ? -1 : stock; }
    public void setStock(Integer stock){ this.stock = stock; }
    public Integer getIsUp(){ return isUp == null ? 0 : isUp; }
    public void setIsUp(Integer isUp){ this.isUp = isUp; }
    public Integer getSort(){ return sort == null ? 0 : sort; }
    public void setSort(Integer sort){ this.sort = sort; }
    public Integer getDeleted(){ return deleted; }
    public void setDeleted(Integer deleted){ this.deleted = deleted; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt){ this.updatedAt = updatedAt; }
}
