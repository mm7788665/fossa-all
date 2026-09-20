package com.txy.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by huyawei 2026/9/10 09:30
 */
@Component
public class WechatHttpClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public String get(String url) {
        return restTemplate.getForObject(url, String.class);
    }
}