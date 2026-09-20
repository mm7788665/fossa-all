package com.txy.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单视图对象（关联玩家、场次）
 */
@Data
public class OrderVO {

    private String id;
    private String playerId;
    private String playerName;
    private String showId;
    private String showTitle;
    private BigDecimal amount;
    private Integer qty;
    private String channel;
    private String status;
    private LocalDateTime createdAt;
}
