package com.txy.dto;

import lombok.Data;

/**
 * Created by huyawei 2026/9/10 09:29
 */
@Data
public class PhoneNumberInfo {
    private String phoneNumber;
    private String purePhoneNumber;
    private String countryCode;
    private Watermark watermark;
    @Data
    // getter / setter
    public static class Watermark {
        private String appid;
        private Long timestamp;
        // getter / setter
    }


}