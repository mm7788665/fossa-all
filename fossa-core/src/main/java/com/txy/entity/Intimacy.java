package com.txy.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 亲密度表（联合业务键 player_id + npc_id）
 */
public class Intimacy implements Serializable {

    private static final long serialVersionUID = 1L;

    private String playerId;
    private String npcId;
    private Integer value;

    /** 逻辑删除：0=未删除 1=已删除 */
    private Integer deleted;

    private LocalDateTime updatedAt;

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public String getNpcId() { return npcId; }
    public void setNpcId(String npcId) { this.npcId = npcId; }
    public Integer getValue() { return value; }
    public void setValue(Integer value) { this.value = value; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
