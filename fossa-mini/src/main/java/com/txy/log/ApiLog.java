package com.txy.log;
import java.lang.annotation.*;
/**
 * Created by huyawei 2026/9/14 09:56
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiLog {
    /**
     * 接口描述
     */
    String value() default "";
}