package com.txy.vo;

import lombok.Data;

/**
 * 控制台统计数据
 */
@Data
public class DashboardVO {

    private Integer npc;
    private Integer show;
    private Integer showOnSale;
    private Integer order;
    private Integer paid;
    private Integer player;
    private Integer notice;
    private Integer intimacy;
    private Double revenue;
    private Integer seats;
    private Integer cap;
    private Integer rate;
    private String appName;
    private String storeName;
}
