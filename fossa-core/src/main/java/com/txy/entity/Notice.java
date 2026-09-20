package com.txy.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告表（字段 top 为关键字，XML 中写作 `top`）
 */
public class Notice implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String cat;
    private Integer top;
    private String content;
    private String author;
    private String status;

    /** 逻辑删除：0=未删除 1=已删除 */
    private Integer deleted;

    private LocalDateTime createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCat() { return cat; }
    public void setCat(String cat) { this.cat = cat; }
    public Integer getTop() { return top; }
    public void setTop(Integer top) { this.top = top; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
