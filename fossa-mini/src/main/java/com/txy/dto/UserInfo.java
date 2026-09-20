package com.txy.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by huyawei 2026/9/10 11:45
 */
@Data
public class UserInfo implements Serializable {
    private String fanId;

    private String nickName;

    private String wxPhoto;

    private String sessionKey;
}
