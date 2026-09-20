package com.txy.security;

import java.lang.annotation.*;

/**
 * 标记"无需登录"的接口（如登录本身、健康检查）。
 * 拦截器遇到这个注解会直接放行，不校验 session。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublicApi {
}
