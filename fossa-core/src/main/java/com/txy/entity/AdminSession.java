package com.txy.entity;

import java.time.LocalDateTime;

/** 登录会话（对应 admin_session） */
public class AdminSession {
    private String token;
    private String user;
    private String name;
    private String roleKey;
    private String ip;
    private String ua;
    private LocalDateTime loginAt;
    private LocalDateTime expireAt;
    private LocalDateTime lastAt;

    public String getToken(){ return token; }
    public void setToken(String v){ this.token = v; }
    public String getUser(){ return user; }
    public void setUser(String v){ this.user = v; }
    public String getName(){ return name == null ? "" : name; }
    public void setName(String v){ this.name = v; }
    public String getRoleKey(){ return roleKey == null ? "viewer" : roleKey; }
    public void setRoleKey(String v){ this.roleKey = v; }
    public String getIp(){ return ip; }
    public void setIp(String v){ this.ip = v; }
    public String getUa(){ return ua; }
    public void setUa(String v){ this.ua = v; }
    public LocalDateTime getLoginAt(){ return loginAt; }
    public void setLoginAt(LocalDateTime v){ this.loginAt = v; }
    public LocalDateTime getExpireAt(){ return expireAt; }
    public void setExpireAt(LocalDateTime v){ this.expireAt = v; }
    public LocalDateTime getLastAt(){ return lastAt; }
    public void setLastAt(LocalDateTime v){ this.lastAt = v; }

    /** 是否已过期 */
    public boolean expired(){ return expireAt != null && expireAt.isBefore(LocalDateTime.now()); }
}
