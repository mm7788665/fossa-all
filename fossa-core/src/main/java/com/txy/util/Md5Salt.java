package com.txy.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Created by huyawei 2026/9/12 11:35
 */
public class Md5Salt {
    // 生成盐 + 加密
    public static String[] encrypt(String rawStr) throws Exception {
        // 1. 生成16位随机盐
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        String saltStr = Base64.getEncoder().encodeToString(salt);

        // 2. 原文 + salt 拼接
        String content = rawStr + saltStr;
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest(content.getBytes());

        // 转16进制
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) sb.append("0");
            sb.append(hex);
        }
        return new String[]{saltStr, sb.toString()};
    }

    // 校验：传入原文、盐、密文
    public static boolean verify(String rawStr, String saltStr, String cipher) throws Exception {
        String content = rawStr + saltStr;
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest(content.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) sb.append("0");
            sb.append(hex);
        }
        return sb.toString().equals(cipher);
    }

    public static void main(String[] args) throws Exception {
        String raw = "osnxU1z_UzZUtenn2WmakwtvLLaE";
        String[] res = encrypt(raw);
        String salt = res[0];
        String md5Val = res[1];
        System.out.println("盐：" + salt);
        System.out.println("MD5(原文+盐)：" + md5Val);
        System.out.println("校验结果：" + verify(raw, salt, md5Val));
    }
}
