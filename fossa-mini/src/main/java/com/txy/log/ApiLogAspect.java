package com.txy.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * Created by huyawei 2026/9/14 09:53
 */
@Slf4j
@Aspect
@Component
public class ApiLogAspect {
    // 切点：标记了@ApiLog注解的方法
    @Pointcut("@annotation(com.txy.log.ApiLog)")
    public void apiLogPointCut(){}

    @Around("apiLogPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        ApiLog apiLog = signature.getMethod().getAnnotation(ApiLog.class);
        String desc = apiLog.value();

        // 获取request
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 请求信息 + 入参
        String url = request.getRequestURI();
        String httpMethod = request.getMethod();
        Object[] args = joinPoint.getArgs();

        log.info("【接口日志】【{}】地址:{}, 请求方式:{}, 入参:{}",
                desc, url, httpMethod, Arrays.toString(args));

        Object result;
        try {
            result = joinPoint.proceed();
            long cost = System.currentTimeMillis() - start;
            log.info("【接口日志】【{}】耗时:{}ms, 出参:{}", desc, cost, result);
        } catch (Throwable e) {
            long cost = System.currentTimeMillis() - start;
            log.error("【接口日志】【{}】异常! 耗时:{}ms", desc, cost, e);
            throw e;
        }
        return result;
    }
}
