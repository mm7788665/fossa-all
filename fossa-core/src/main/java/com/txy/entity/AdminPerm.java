package com.txy.entity;

/** 权限点（对应 admin_perm） */
public class AdminPerm {
    private String permCode;
    private String permName;
    private String module;
    private Integer sort;

    public String getPermCode(){ return permCode; }
    public void setPermCode(String v){ this.permCode = v; }
    public String getPermName(){ return permName == null ? "" : permName; }
    public void setPermName(String v){ this.permName = v; }
    public String getModule(){ return module == null ? "" : module; }
    public void setModule(String v){ this.module = v; }
    public Integer getSort(){ return sort == null ? 0 : sort; }
    public void setSort(Integer v){ this.sort = v; }
}
