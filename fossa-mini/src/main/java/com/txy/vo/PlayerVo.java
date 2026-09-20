package com.txy.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by huyawei 2026/9/10 14:46
 */
@Data
public class PlayerVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 玩家ID me/p1..p8 */
    private String id;

    /** 昵称 */
    private String name;

    /** 标签：剧本杀老手… */
    private String tag;

    /** emoji */
    private String avatar;

    /** 手机号（脱敏） */
    private String phone;

    /** VIP/普通 */
    private String vip;

    /** 微信 openid */
//    private String wxOpenId;

    /** sessionKey */
    private String sessionKey;
}