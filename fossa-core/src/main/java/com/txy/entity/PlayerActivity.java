package com.txy.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 玩家活跃表
 */
public class PlayerActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    private String playerId;
    private Integer sessions;
    private Integer minutes;

    /** 逻辑删除：0=未删除 1=已删除 */
    private Integer deleted;

    private LocalDateTime updatedAt;

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public Integer getSessions() { return sessions; }
    public void setSessions(Integer sessions) { this.sessions = sessions; }
    public Integer getMinutes() { return minutes; }
    public void setMinutes(Integer minutes) { this.minutes = minutes; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
