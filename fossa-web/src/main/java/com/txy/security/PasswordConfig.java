package com.txy.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器（供其他地方注入使用）
 *
 * ⚠️ 如果启动报 "No qualifying bean of type PasswordEncoder"，
 *    说明没引 spring-security-crypto 依赖，见 PasswordKit 注释里的 maven 坐标。
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }
}
