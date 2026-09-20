package com.txy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.txy.mapper")
//@PropertySource("classpath:datasource.properties")
public class DataSourceConfig {
}
