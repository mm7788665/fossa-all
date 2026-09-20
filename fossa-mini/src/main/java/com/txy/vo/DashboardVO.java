package com.txy.fossa.web.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 控制台统计 —— 对齐前端 Dashboard 卡片 + 人气榜 + 上座率
 */
@Data
public class DashboardVO {

    // 概览数字（对应 v_dashboard）
    private int npcCount;
    private int showCount;
    private int showOnSale;
    private int orderCount;
    private int paidCount;
    private int playerCount;
    private long revenue;
    private int seatsTaken;
    private int seatsTotal;
    private int noticePublished;
    private int intimacy;

    // NPC 人气榜
    private List<Map<String, Object>> popularity;
    // 场次上座
    private List<Map<String, Object>> seatRate;
    // 最近订单
    private List<Map<String, Object>> recentOrders;
}
