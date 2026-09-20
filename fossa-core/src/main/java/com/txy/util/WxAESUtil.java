package com.txy.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Created by huyawei 2026/9/10 09:30
 */

public class WxAESUtil {

    /**
     * 解密微信加密数据
     * @param encryptedData 加密数据（Base64）
     * @param sessionKey    会话密钥（Base64）
     * @param iv            偏移量（Base64）
     */
    public static String decrypt(String encryptedData, String sessionKey, String iv) {
        try {
            byte[] dataByte = Base64.getDecoder().decode(encryptedData);
            byte[] keyByte = Base64.getDecoder().decode(sessionKey);
            byte[] ivByte = Base64.getDecoder().decode(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(keyByte, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivByte);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] result = cipher.doFinal(dataByte);

            return new String(result, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("微信数据解密失败", e);
        }
    }
}
