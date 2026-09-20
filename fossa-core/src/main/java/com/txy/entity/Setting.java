package com.txy.entity;

import java.io.Serializable;

/**
 * 系统设置表（字段 k 为保留字，XML 中写作 `k`）
 */
public class Setting implements Serializable {

    private static final long serialVersionUID = 1L;

    private String k;
    private String v;
    private String label;

    /** 逻辑删除：0=未删除 1=已删除 */
    private Integer deleted;

    public String getK() { return k; }
    public void setK(String k) { this.k = k; }
    public String getV() { return v; }
    public void setV(String v) { this.v = v; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
