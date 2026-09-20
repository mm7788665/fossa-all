package com.txy.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 场次视图对象（关联 NPC 信息）
 */
@Data
public class ShowVO {

    private String id;
    private String title;
    private String npcId;
    private String npcName;
    private String npcIcon;
    private LocalDate date;
    private String time;
    private String city;
    private String venue;
    private BigDecimal price;
    private Integer capacity;
    private Integer taken;
    private String status;
}
