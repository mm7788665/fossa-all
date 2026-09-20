package com.txy.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.txy.client.WechatHttpClient;
import com.txy.client.WechatProperties;
import com.txy.dto.Code2SessionResponse;
import com.txy.dto.PhoneNumberInfo;
import com.txy.util.WxAESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by huyawei 2026/9/10 09:31
 */
@Service
public class WechatService {

    @Autowired
    private WechatProperties properties;

    @Autowired
    private WechatHttpClient httpClient;

    /**
     * code2Session 换 openid + session_key
     */
    public Code2SessionResponse code2Session(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session"
                + "?appid=" + properties.getAppid()
                + "&secret=" + properties.getSecret()
                + "&js_code=" + code
                + "&grant_type=" + properties.getGrantType();

        String result = httpClient.get(url);
        ObjectMapper objectMapper = new ObjectMapper();
        // 用你项目里的 JSON 工具反序列化（Fastjson / Jackson 都行）
        Code2SessionResponse response = null;
        try {
            response = objectMapper.readValue(
                    result,
                    new TypeReference<Code2SessionResponse>() {}
                );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    /**
     * 解密手机号
     */
    public PhoneNumberInfo decryptPhoneNumber(String encryptedData, String sessionKey, String iv) {
        String json = WxAESUtil.decrypt(encryptedData, sessionKey, iv);
        ObjectMapper objectMapper = new ObjectMapper();
        // 用你项目里的 JSON 工具反序列化（Fastjson / Jackson 都行）
        PhoneNumberInfo response = null;
        try {
            response = objectMapper.readValue(
                    json,
                    new TypeReference<PhoneNumberInfo>() {}
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}
