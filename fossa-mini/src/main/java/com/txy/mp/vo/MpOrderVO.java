package com.txy.mp.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/** 小程序 · 订单视图 */
@Data
public class MpOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String showId;
    private String showTitle;
    private String showDate;
    private String showTime;
    private String city;
    private String venue;
    private String userName;
    private BigDecimal amount;
    private Integer qty;
    private String status;
    private String createdAt;
}
