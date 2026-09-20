package com.txy.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 类RedisUtils的实现描述：redis工具类
 *
 * @author wuzh@hyxt.com 2017/7/4 16:16
 * @since JDK 1.7
 */
public class RedisUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisUtils.class);

    /**
     * 构建key
     *
     * @param key
     * @param args
     * @return
     */
    public static String buildKey(String key, String... args) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        if (args == null || args.length == 0) {
            return key;
        }

        StringBuilder result = new StringBuilder(key);
        for (String arg : args) {
            if (StringUtils.isNotBlank(arg)) {
                result.append(arg).append(RedisConstant.COLON);
            }
        }
        return StringUtils.substringBeforeLast(result.toString(), RedisConstant.COLON);
    }
}
