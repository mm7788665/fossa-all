package com.txy.mp.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/** 小程序 · 玩家信息视图 */
@Data
public class MpPlayerVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String tag;
    private String avatar;
    private String phone;
    private String vip;
    private String status;
    private Map<String, Integer> intimacy;
    private Integer intimacyTotal;
}
