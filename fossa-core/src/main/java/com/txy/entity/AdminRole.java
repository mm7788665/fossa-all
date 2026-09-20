package com.txy.entity;

import java.util.ArrayList;
import java.util.List;

/** 后台角色（对应 admin_role） */
public class AdminRole {
    private String roleKey;
    private String roleName;
    private String remark;
    private Integer builtin;
    private Integer sort;
    private Integer deleted;
    /** 该角色拥有的权限码（非表字段，查询时填充） */
    private List<String> perms = new ArrayList<>();

    public String getRoleKey(){ return roleKey; }
    public void setRoleKey(String v){ this.roleKey = v; }
    public String getRoleName(){ return roleName == null ? "" : roleName; }
    public void setRoleName(String v){ this.roleName = v; }
    public String getRemark(){ return remark == null ? "" : remark; }
    public void setRemark(String v){ this.remark = v; }
    public Integer getBuiltin(){ return builtin == null ? 0 : builtin; }
    public void setBuiltin(Integer v){ this.builtin = v; }
    public Integer getSort(){ return sort == null ? 0 : sort; }
    public void setSort(Integer v){ this.sort = v; }
    public Integer getDeleted(){ return deleted == null ? 0 : deleted; }
    public void setDeleted(Integer v){ this.deleted = v; }
    public List<String> getPerms(){ return perms; }
    public void setPerms(List<String> perms){ this.perms = perms == null ? new ArrayList<>() : perms; }
}
