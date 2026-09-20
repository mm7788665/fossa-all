package com.txy.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * NPC 角色表
 * 说明：不使用 MyBatis-Plus 注解，纯 POJO；
 *      保留字字段（desc）在 XML 的 SQL 里统一加反引号处理。
 */
public class Npc implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String icon;
    private String cover;
    private String role;
    private String faction;
    private String campLabel;
    private String power;
    private String ability;
    private String cost;
    private String tags;
    /** 对应列 `desc`（MySQL 保留字），XML 中均写作 `desc` */
    private String desc;
    private String quotes;
    private String hookAction;
    private String hookContrast;
    private String hookFragile;
    private String hookSpeech;
    private Integer sort;
    private String status;

    /** 逻辑删除：0=未删除 1=已删除 */
    private Integer deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getFaction() { return faction; }
    public void setFaction(String faction) { this.faction = faction; }
    public String getCampLabel() { return campLabel; }
    public void setCampLabel(String campLabel) { this.campLabel = campLabel; }
    public String getPower() { return power; }
    public void setPower(String power) { this.power = power; }
    public String getAbility() { return ability; }
    public void setAbility(String ability) { this.ability = ability; }
    public String getCost() { return cost; }
    public void setCost(String cost) { this.cost = cost; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public String getQuotes() { return quotes; }
    public void setQuotes(String quotes) { this.quotes = quotes; }
    public String getHookAction() { return hookAction; }
    public void setHookAction(String hookAction) { this.hookAction = hookAction; }
    public String getHookContrast() { return hookContrast; }
    public void setHookContrast(String hookContrast) { this.hookContrast = hookContrast; }
    public String getHookFragile() { return hookFragile; }
    public void setHookFragile(String hookFragile) { this.hookFragile = hookFragile; }
    public String getHookSpeech() { return hookSpeech; }
    public void setHookSpeech(String hookSpeech) { this.hookSpeech = hookSpeech; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
