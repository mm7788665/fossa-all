package com.txy.mp.vo;

import lombok.Data;
import java.io.Serializable;

/** 小程序 · 抽卡奖品视图 */
@Data
public class MpGachaPrizeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String tag;
    private Integer rarity;
    private String icon;
}
