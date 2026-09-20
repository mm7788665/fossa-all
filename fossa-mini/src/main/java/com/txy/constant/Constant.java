package com.txy.constant;

/**
 * 类Constant的实现描述：常量信息
 *
 * @author wuzh@hyxt.com 2017/7/24 10:50
 * @since JDK 1.7
 */
public class Constant {

    /**
     * 获取 OPENID 路径
     */
    public final static String GET_OPENID_URL = "GET_OPENID_URL";

    /**
     * Header请求参数中的用户会话sessionId
     */
    public static final String SESSION_ID = "sessionId";
    public static final String SESSION_KEY = "sessionKey";
    public final static String STORE_ID = "storeId";
    public final static String SHOP_ID = "shopId";
    public final static String ACTIVITY_ID = "activityId";

    /**
     * 中国所在区域的ID
     */
    public final static String ROOT_CIRCLEID = "1";

    /**
     * 接口请求参数
     */
    public final static String LOGGER_REQUEST = "LOGGER_REQUEST";
    /**
     * 接口请求开始时间
     */
    public final static String LOGGER_REQUEST_START = "LOGGER_REQUEST_START";
    /**
     * 接口响应信息
     */
    public final static String LOGGER_RESPONSE = "LOGGER_RESPONSE";


    /**
     * 点赞 - 动态许愿类型
     */
    public static final int DYNAMIC_DIANZAN = 1;
    /**
     * 踩价格 - 动态许愿类型
     */
    public static final int DYNAMIC_CAIJIA = 2;
    /**
     * 投票  - 动态许愿类型
     */
    public static final int DYNAMIC_VOTE = 3;
    /**
     * 许愿 - 动态许愿类型
     */
    public static final int DYNAMIC_WISH = 4;
    /**
     * 送礼 - 动态许愿类型
     */
    public static final int DYNAMIC_GIFTS = 5;
    /**
     * 秒杀 - 动态秒杀类型
     */
    public static final int DYNAMIC_SECKILL = 7;
    /**
     * 秒杀 - 动态预售类型
     */
    public static final int DYNAMIC_PRESELL = 8;
    /**
     * 动态套餐
     */
    public static final int DYNAMIC_SET_MEAL = 10;
    /**
     * 满减 - 动态满减类型
     */
    public static final int DYNAMIC_MINUS = 9;
    /**
     * 限时折扣
     */
    public static final int DYNAMIC_LIMIT = 11;
    /**
     * 抱团购  t_dynamic_huddle
     */
    public static final int DYNAMIC_HUDDLE = 12;
    /**
     * 卡券
     */
    public static final int DYNAMIC_VOUCHER = 13;
    /**
     * 游客默认的unionId
     */
    public static final String GUEST_UNIONID = "linke1ge4wuxiaodeunionid00";

    /**
     * 游客默认的buyerId
     */
    public static final Long GUEST_BUYER_ID = -111111111L;
}
