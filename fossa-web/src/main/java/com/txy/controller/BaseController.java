package com.txy.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by huyawei 2026/9/12 21:05
 */
@Component
public class BaseController {
    @Resource(name = "fossaStringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    protected final Logger logger = LoggerFactory.getLogger(getClass());
//    protected void refreshCurrentBuyerVo(String sessionId, PlayerVo playerVo) {
//        playerVo.setSessionKey(sessionId);
//        // 将用户信息放入到Redis缓存中，60-90分钟内有效
//        String key = RedisUtils.buildKey("nds:shop:buyer:auth:", sessionId);
//        long timeout = ConstantRedisKey.AUTH_DEFAULT_TIMEOUT + RandomUtils.nextInt(1,30) ;
//
//        stringRedisTemplate.opsForValue().set(key, JSON.toJSONString(playerVo), timeout, TimeUnit.MINUTES);
//
//
//    }

    /**
     * 获取当前认证的用户信息
     *
     * @param sessionId 用户会话Id
     * @return
     */
//    protected PlayerVo getCurrentBuyerVo( String sessionId) {
//        String key = RedisUtils.buildKey(ConstantRedisKey.EL_SHOP_BUYER_AUTH, sessionId);
//        String s = stringRedisTemplate.opsForValue().get(key);
//        if (s != null) {
//            // 刷新用户信息缓存有效期，60-90分钟内有效
//            long timeout = ConstantRedisKey.AUTH_DEFAULT_TIMEOUT + RandomUtils.nextInt(1,30) ;
//            stringRedisTemplate.opsForValue().set(key,s, timeout, TimeUnit.MINUTES);
//        }
//        PlayerVo playerVo = JSON.parseObject(s, PlayerVo.class);
//        return playerVo;
//    }
}
