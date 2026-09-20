package com.txy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by huyawei 2026/9/15 10:08
 */
@SpringBootApplication
@MapperScan("com.txy.mapper")
@PropertySource("classpath:datasource.properties")
public class ApplicationWeb extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationWeb.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ApplicationWeb.class);
    }
}