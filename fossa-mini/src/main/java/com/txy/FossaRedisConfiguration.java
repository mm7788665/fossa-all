package com.txy;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class FossaRedisConfiguration {

    @Bean(name = {"fossaStringRedisTemplate"})
    public StringRedisTemplate stringRedisTemplate(@Value("${fossa.redis.host}") String hostName,
                                                   @Value("${fossa.redis.port}") int port,
                                                   @Value("${fossa.redis.password}") String password,
                                                   @Value("${fossa.redis.index}") int index,
                                                   @Value("${fossa.redis.pool.max-idle}") int maxIdle,
                                                   @Value("${fossa.redis.pool.min-idle}") int minIdle,
                                                   @Value("${fossa.redis.pool.max-active}") int maxActive,
                                                   @Value("${fossa.redis.pool.max-wait}") long maxWaitMillis) {
        JedisConnectionFactory jedis = new JedisConnectionFactory();
        jedis.setHostName(hostName);
        jedis.setPort(port);
        if (!StringUtils.isEmpty(password)) {
            jedis.setPassword(password);
        }
        if (index != 0) {
            jedis.setDatabase(index);
        }
        jedis.setPoolConfig(poolConfig(maxIdle, minIdle, maxActive, maxWaitMillis));
        // 初始化连接pool  
        jedis.afterPropertiesSet();

        StringRedisTemplate temple = new StringRedisTemplate();
        temple.setConnectionFactory(jedis);
        return temple;
    }

    @Bean(name = {"jdkRedisTemplate", "fossaRedisTemplate"})
    public RedisTemplate jdkRedisTemplate(@Value("${fossa.redis.host}") String hostName,
                                          @Value("${fossa.redis.port}") int port,
                                          @Value("${fossa.redis.password}") String password,
                                          @Value("${fossa.redis.index}") int index,
                                          @Value("${fossa.redis.pool.max-idle}") int maxIdle,
                                          @Value("${fossa.redis.pool.min-idle}") int minIdle,
                                          @Value("${fossa.redis.pool.max-active}") int maxActive,
                                          @Value("${fossa.redis.pool.max-wait}") long maxWaitMillis) {
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
        connectionFactory.setHostName(hostName);
        connectionFactory.setPort(port);
        if (!StringUtils.isEmpty(password)) {
            connectionFactory.setPassword(password);
        }
        if (index != 0) {
            connectionFactory.setDatabase(index);
        }
        connectionFactory.setPoolConfig(poolConfig(maxIdle, minIdle, maxActive, maxWaitMillis));
        // 初始化连接pool
        connectionFactory.afterPropertiesSet();

        RedisTemplate redisTemplate = new RedisTemplate();
        JdkSerializationRedisSerializer serializer = new JdkSerializationRedisSerializer();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer);
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

    public JedisPoolConfig poolConfig(int maxIdle, int minIdle, int maxTotal,
                                      long maxWaitMillis) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        poolConfig.setTestOnBorrow(true);
        return poolConfig;
    }
}  