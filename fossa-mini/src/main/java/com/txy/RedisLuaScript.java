package com.txy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * Created by andy on 2017/3/28.
 */
@Configuration
public class RedisLuaScript {


    @Bean
    public DefaultRedisScript<Long> defaultRedisScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<Long>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(
                "/META-INF/lua/redis_request_queue.lua")));
        redisScript.setResultType(Long.class);
        return redisScript;
    }
}
