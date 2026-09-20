package com.txy.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 场次表（表名 `show` 为 MySQL 关键字，SQL 中一律反引号）
 */
@Data
public class Show implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String npcId;
    /** 列名 date 为关键字，XML 中写作 `date` */
    private LocalDate date;
    /** 列名 time 为关键字，XML 中写作 `time` */
    private String time;
    private String city;
    private String venue;
    private BigDecimal price;
    private Integer capacity;
    private Integer taken;
    private String status;

    /** 逻辑删除：0=未删除 1=已删除 */
    private Integer deleted;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime saleStartAt;

}
