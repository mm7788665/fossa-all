package com.txy.dto;

import lombok.Data;

/**
 * Created by huyawei 2026/9/10 09:28
 */
@Data
public class Code2SessionResponse {
    private String openid;
    private String session_key;
    private String unionid;
    private Integer errcode;
    private String errmsg;

}