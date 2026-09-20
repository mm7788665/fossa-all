package com.txy.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by huyawei 2026/9/10 09:27
 */
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatProperties {
    private String appid;
    private String secret;
    private String grantType;

    // getter / setter
    public String getAppid() { return appid; }
    public void setAppid(String appid) { this.appid = appid; }
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    public String getGrantType() { return grantType; }
    public void setGrantType(String grantType) { this.grantType = grantType; }
}