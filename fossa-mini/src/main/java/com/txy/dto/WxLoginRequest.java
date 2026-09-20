package com.txy.dto;

import lombok.Data;

/**
 * Created by huyawei 2026/9/10 09:28
 */
@Data
public class WxLoginRequest {
    private String code;          // wx.login() 拿到的 code
    private String encryptedData;  // 手机号加密数据（可选）
    private String iv;             // 偏移量（可选）


}
