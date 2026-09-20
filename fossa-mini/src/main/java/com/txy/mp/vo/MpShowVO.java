package com.txy.mp.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/** 小程序 · 场次视图（含余座） */
@Data
public class MpShowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String npcId;
    private String npcName;
    private String npcCover;
    private String date;   // yyyy-MM-dd（避免时区/时间戳问题）
    private String time;
    private String city;
    private String venue;
    private BigDecimal price;
    private Integer capacity;
    private Integer taken;
    private Integer remain;
    private String status;
}
