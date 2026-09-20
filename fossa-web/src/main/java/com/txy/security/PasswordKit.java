package com.txy.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码工具：BCrypt 加密 + 平滑迁移
 *
 * 【为什么要平滑迁移】
 * 线上库里已经是明文（123456），不可能让所有人重新设密码。
 * 所以登录时兼容两种：
 *   ① 库里是 BCrypt（以 $2a$ / $2b$ / $2y$ 开头）→ 用 BCrypt 校验
 *   ② 库里是明文 → 直接比对；比对了成功就【顺手把明文升级成 BCrypt】存回去
 * 用户无感知，跑一段时间库里就全是密文了。
 *
 * 【依赖】只需 crypto 模块，不用引整个 spring-security：
 *   <dependency>
 *     <groupId>org.springframework.security</groupId>
 *     <artifactId>spring-security-crypto</artifactId>
 *   </dependency>
 */
public final class PasswordKit {

    /** BCrypt 强度：10 是官方推荐，越高越慢。12 会明显变慢，一般 10 够用 */
    private static final int STRENGTH = 10;

    private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder(STRENGTH);

    private PasswordKit(){}

    /** 生成密文（新用户、改密码时用） */
    public static String encode(String raw){
        if (raw == null) return "";
        return ENCODER.encode(raw);
    }

    /** 校验：自动识别明文 / BCrypt */
    public static boolean matches(String raw, String stored){
        if (raw == null || stored == null) return false;
        if (isBcrypt(stored)) {
            try{ return ENCODER.matches(raw, stored); }
            catch(Exception e){ return false; }   // 密文格式损坏时按失败处理，不崩
        }
        /* 明文（存量数据）：直接比对 */
        return raw.equals(stored);
    }

    /** 判断是否是 BCrypt 密文 */
    public static boolean isBcrypt(String s){
        if (s == null || s.length() != 60) return false;
        return s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$");
    }

    /** 密码强度校验：至少 6 位。想更严可改这里 */
    public static String checkStrength(String raw){
        if (raw == null || raw.length() < 6) return "密码至少 6 位";
        if (raw.length() > 64) return "密码不能超过 64 位";
        /* 弱密码黑名单，按需扩充 */
        String[] weak = {"123456","12345678","111111","000000","abc123","password","admin","qwerty"};
        String low = raw.toLowerCase();
        for (String w : weak) if (low.equals(w)) return "密码过于简单，请换一个";
        return null;   // null = 通过
    }
}
