package com.txy;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.Serializable;


/**
 * Created by huyawei 2026/9/15 10:08
 */

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.txy")
@PropertySource({"classpath:datasource.properties"})
@EnableAsync
public class Application {


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public BeanNameAutoProxyCreator beanNameAutoProxyCreator() {
        BeanNameAutoProxyCreator beanNameAutoProxyCreator = new BeanNameAutoProxyCreator();
        //设置要创建代理的那些Bean的名字
        beanNameAutoProxyCreator.setBeanNames("*RedisService");
        //设置拦截链名字(这些拦截器是有先后顺序的)
        beanNameAutoProxyCreator.setInterceptorNames("autoproxyInterceptor");
        return beanNameAutoProxyCreator;
    }

    /**
     * 使CacheComponent的redisTemplate组件的key使用StringRedisSerializer而非默认的JdkSerializationRedisSerializer
     * 避免key出现字节码的情况
     *
     * @param factory redis链接
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<Serializable, Serializable> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<Serializable, Serializable> template = new RedisTemplate<Serializable, Serializable>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(template.getStringSerializer());
        template.afterPropertiesSet();
        return template;
    }
}