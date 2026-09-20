package com.txy.entity;

/** 管理后台账号（对应 admin_account） */
public class AdminAccount {
    private String user;
    private String pass;
    /** 角色标识 role_key，如 super / editor / viewer */
    private String role;
    private String name;
    private java.time.LocalDateTime lastLogin;
    private java.time.LocalDateTime createdAt;
    private Integer deleted;

    public String getUser(){ return user; }
    public void setUser(String user){ this.user = user; }
    public String getPass(){ return pass; }
    public void setPass(String pass){ this.pass = pass; }
    public String getRole(){ return role == null ? "viewer" : role; }
    public void setRole(String role){ this.role = role; }
    public String getName(){ return name == null ? "" : name; }
    public void setName(String name){ this.name = name; }
    public java.time.LocalDateTime getLastLogin(){ return lastLogin; }
    public void setLastLogin(java.time.LocalDateTime v){ this.lastLogin = v; }
    public java.time.LocalDateTime getCreatedAt(){ return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime v){ this.createdAt = v; }
    public Integer getDeleted(){ return deleted == null ? 0 : deleted; }
    public void setDeleted(Integer deleted){ this.deleted = deleted; }
}
