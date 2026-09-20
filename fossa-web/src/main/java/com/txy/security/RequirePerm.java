package com.txy.security;

import java.lang.annotation.*;

/**
 * 方法级权限注解。
 *
 * 用法：
 *   @RequirePerm("npc:edit")        → 需要有 npc:edit 权限
 *   @RequirePerm(value="order:delete", desc="删除订单")
 *
 * 不加注解的接口：只要登录就能访问（拦截器仍会校验 session）。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePerm {

    /** 需要的权限码 */
    String value();

    /** 模块名（用于审计日志的中文模块名），留空则自动从 URI 推断 */
    String module() default "";

    /** 动作描述（用于审计日志摘要），留空则按 HTTP 方法推断 */
    String desc() default "";
}
