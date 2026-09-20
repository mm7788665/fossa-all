package com.txy.entity;

import java.time.LocalDateTime;

/** 操作审计日志（对应 admin_audit_log） */
public class AdminAuditLog {
    private String id;
    private String user;
    private String name;
    private String module;
    private String moduleName;
    private String action;
    private String targetId;
    private String summary;
    private String params;
    private String method;
    private String uri;
    private String ip;
    private Integer success;
    private String msg;
    private Integer costMs;
    private LocalDateTime createdAt;

    public static AdminAuditLog of(String user, String name, String module, String moduleName,
                                   String action, String targetId, String summary, String params,
                                   String method, String uri, String ip){
        AdminAuditLog l = new AdminAuditLog();
        l.id = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        l.user = user; l.name = name; l.module = module; l.moduleName = moduleName;
        l.action = action; l.targetId = targetId; l.summary = summary; l.params = params;
        l.method = method; l.uri = uri; l.ip = ip;
        l.success = 1; l.costMs = 0;
        return l;
    }

    public String getId(){ return id; }
    public void setId(String v){ this.id = v; }
    public String getUser(){ return user; }
    public void setUser(String v){ this.user = v; }
    public String getName(){ return name; }
    public void setName(String v){ this.name = v; }
    public String getModule(){ return module; }
    public void setModule(String v){ this.module = v; }
    public String getModuleName(){ return moduleName; }
    public void setModuleName(String v){ this.moduleName = v; }
    public String getAction(){ return action; }
    public void setAction(String v){ this.action = v; }
    public String getTargetId(){ return targetId; }
    public void setTargetId(String v){ this.targetId = v; }
    public String getSummary(){ return summary; }
    public void setSummary(String v){ this.summary = v; }
    public String getParams(){ return params; }
    public void setParams(String v){ this.params = v; }
    public String getMethod(){ return method; }
    public void setMethod(String v){ this.method = v; }
    public String getUri(){ return uri; }
    public void setUri(String v){ this.uri = v; }
    public String getIp(){ return ip; }
    public void setIp(String v){ this.ip = v; }
    public Integer getSuccess(){ return success == null ? 1 : success; }
    public void setSuccess(Integer v){ this.success = v; }
    public String getMsg(){ return msg; }
    public void setMsg(String v){ this.msg = v; }
    public Integer getCostMs(){ return costMs == null ? 0 : costMs; }
    public void setCostMs(Integer v){ this.costMs = v; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public void setCreatedAt(LocalDateTime v){ this.createdAt = v; }
}
