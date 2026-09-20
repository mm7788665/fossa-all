package com.txy.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** 稀有度定义与基础概率 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GachaRarity {

    private String rarityKey;    // SSR / SR / R / N
    private String rarityName;   // 显示名
    private String label;        // 中文标签：特典 / 稀有 / 精良 / 普通
    private String color;        // 主题色
    private BigDecimal rate;     // 基础概率（百分比）
    private Integer sortOrder;   // 越大越稀有

    public GachaRarity() {}

    public String getRarityKey(){ return rarityKey; }
    public void setRarityKey(String rarityKey){ this.rarityKey = rarityKey; }
    public String getRarityName(){ return rarityName; }
    public void setRarityName(String rarityName){ this.rarityName = rarityName; }
    public String getLabel(){ return label; }
    public void setLabel(String label){ this.label = label; }
    public String getColor(){ return color; }
    public void setColor(String color){ this.color = color; }
    public BigDecimal getRate(){ return rate; }
    public void setRate(BigDecimal rate){ this.rate = rate; }
    public Integer getSortOrder(){ return sortOrder == null ? 0 : sortOrder; }
    public void setSortOrder(Integer sortOrder){ this.sortOrder = sortOrder; }
}
