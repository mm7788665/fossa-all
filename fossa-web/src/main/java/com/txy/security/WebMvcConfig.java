package com.txy.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册后台拦截器
 *
 * 拦截 /api/**，但放行：
 *   - /api/auth/login（登录本身）
 *   - /api/auth/captcha 之类（如有）
 *   - /mini/**（小程序接口，走另一套鉴权，不受影响）
 *
 * ⚠️ 如果你的项目已有 WebMvcConfigurer（比如配了 Cors），
 *    把这个 addInterceptors 合并到你自己的配置类里即可，不要建两份。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor interceptor;

    public WebMvcConfig(AdminAuthInterceptor interceptor){ this.interceptor = interceptor; }

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/**")            // 后台接口全拦
                .excludePathPatterns(
                        "/api/auth/login",             // 登录（自身带 @PublicApi，这里双保险）
                        "/api/login",                  // 兼容旧登录路径
                        "/api/auth/logout"
                );
        /* 小程序接口 /mini/** 不拦（用 userId 鉴权，另有一套） */
    }
}
