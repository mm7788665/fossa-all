package com.txy.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * NPC 人气表
 */
public class NpcPopularity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String npcId;
    private Integer votes;
    private Integer weight;

    /** 逻辑删除：0=未删除 1=已删除 */
    private Integer deleted;

    private LocalDateTime updatedAt;

    public String getNpcId() { return npcId; }
    public void setNpcId(String npcId) { this.npcId = npcId; }
    public Integer getVotes() { return votes; }
    public void setVotes(Integer votes) { this.votes = votes; }
    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
