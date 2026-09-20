package com.txy.constant;

import java.util.concurrent.TimeUnit;

/**
 * 类ConstantRedisKey的实现描述：缓存key
 *
 * @author wuzh@hyxt.com 2017/8/2 15:45
 * @since JDK 1.7
 */
public class ConstantRedisKey {

    /**
     * 认证默认缓存时间：60分钟 + 一个1 到30之间的随机数
     */
    public static final long AUTH_DEFAULT_TIMEOUT = 24 * 60;
    /**
     * 默认缓存时间：2小时
     */
    public static final long DEFAULT_TIMEOUT = 2;

    /**
     * 认证缓存时间类型
     */
    public static final TimeUnit AUTH_DEFAULT_TIMEOUT_TimeUnit = TimeUnit.HOURS;

    /**
     * 微信网页
     */
    public static final String EL_SHOP_WEIXIN_CONFIG = "nds:shop:weixin:config:";

    /**
     * 门店信息
     */
    public static final String EL_SHOP_STORE_SIMPLIFIED = "nds:shop:store:simplified:";

    /**
     * 用户信息
     */
    public static final String EL_SHOP_BUYER = "nds:shop:buyer:";
    /**
     * 用户信息 - 验证/登录后的用户信息
     */
    public static final String EL_SHOP_BUYER_AUTH = "nds:shop:buyer:auth:";
    /**
     * 用户信息 - 验证/登录后的用户信息
     */
    public static final String EL_SHOP_BUYER_AUTH_CODE = "nds:shop:buyer:auth:code:";
    /**
     * 用户信息 - 验证/登录后的用户信息
     */
    public static final String EL_SHOP_BUYER_AUTH_MIDA = "nds:shop:buyer:authForMida:";
    /**
     * 用户信息 - 用户注册、重置密码验证码
     */
    public static final String EL_SHOP_BUYER_VERIFY = "nds:shop:buyer:verify";

    /**
     * 地址列表，包含省、市、区县信息
     */
    public final static String EL_SHOP_ADDRESS_CIRCLE_LIST = "nds:shop:address:circle:list:";
    /**
     * 地址信息 - 省份列表
     */
    public final static String EL_SHOP_ADDRESS_PROVINCE_LIST = "nds:shop:address:province:list:";
    /**
     * 地址信息 - 根据省份Id查询市列表
     */
    public final static String EL_SHOP_ADDRESS_CITY_LIST = "nds:shop:address:city:list:";
    /**
     * 地址信息 - 根据市Id查询区县列表
     */
    public final static String EL_SHOP_ADDRESS_COUNTY_LIST = "nds:shop:address:county:list:";
    /**
     * 地址信息 - 省份明细
     */
    public final static String EL_SHOP_ADDRESS_PROVINCE_DETAIL = "nds:shop:address:province:detail:";
    /**
     * 地址信息 - 根据市Id查询市明细
     */
    public final static String EL_SHOP_ADDRESS_CITY_DETAIL = "nds:shop:address:city:detail:";
    /**
     * 地址信息 - 根据区县Id查询区县列表
     */
    public final static String EL_SHOP_ADDRESS_COUNTY_DETAIL = "nds:shop:address:county:detail:";

    /**
     * 配送信息 - 商家配送模板
     */
    public final static String EL_SHOP_DELIVERY_SHOP_REGION_TEMPLATE = "nds:shop:shopDelivery:regionTemplate:";

    /**
     * 确认订单
     */
    public final static String EL_SHOP_ORDER_CONFIRM = "nds:shop:order:confirm:";


    /*-------------------------- 微信配置 ------------------------------*/
    /**
     * 微信 - 根据商家Id获取微信配置
     */
    public final static String EL_SHOP_WEIXIN_CONFIG_ShopId = "nds:shop:weixin:config:shopId:";
    /**
     * 微信 - 根据门店Id获取微信配置
     */
    public final static String EL_SHOP_WEIXIN_CONFIG_StoreId = "nds:shop:weixin:config:storeId:";

    /**
     * 基础信息 - 根据门店Id获取商家Id
     */
    public final static String EL_SHOP_Simplified_ShopId = "nds:shop:Simplified:shopId:";

    /**
     * storeVo - 根据门店Id获取门店信息
     */
    public final static String EL_SHOP_STORE_STOREVO = "nds:shop:store:storeId:";

    /**
     * 门店信息 - 根据商家Id获取门店列表
     */
    public final static String EL_SHOP_STORE_LIST_TopStoresByShopId = "nds:shop:store:list:TopStoresByShopId:";
    /**
     * 门店信息 - 根据商家ID，城市名称查询门店列表
     */
    public final static String EL_SHOP_STORE_LIST_StoreByCityName = "nds:shop:store:list:StoreByCityName:";
    /**
     * 查询出商家所有可用的门店列表
     */
    public static final String STORE_LIST_ENABLE = "nds:shop:store:enable:";

    /**
     * 商品信息 - 根据商品Id查询商品信息
     */
    public static final String EL_SHOP_GOODS_INFO_findSimpleGoodsInfoDetailResponse = "nds:shop:goods:info:findSimpleGoodsInfoDetailResponse";
    /**
     * 商品sku信息 - 根据商品编号获取商品Sku列表（不包含sku属性）
     */
    public static final String EL_SHOP_GOODS_SKU_getGoodsSkuByGoodsId = "nds:shop:goods:sku:getGoodsSkuByGoodsId";
    /**
     * 商品sku属性信息 - 根据商品基础Id获取商品属性集合信息
     */
    public static final String EL_SHOP_GOODS_SKU_ATTRIBUTE_getGoodsSkuAttributesByGoodsBaseId = "nds:shop:goods:sku:attribute:getGoodsSkuAttributesByGoodsBaseId";
    /**
     * 商品sku属性信息 - 根据商品基础Id、商品SKU基础id获取商品属性集合信息
     */
    public static final String EL_SHOP_GOODS_SKU_ATTRIBUTE_getGoodsSkuAttributesByGoodsBaseIdAndSkuBaseId = "nds:shop:goods:sku:attribute:getGoodsSkuAttributesByGoodsBaseIdAndSkuBaseId";
    /**
     * 商品图片 - 根据商品同步Id获取商品图片列表
     */
    public static final String EL_SHOP_GOODS_PIC_findGoodsPicByGoodsSyncId = "nds:shop:goods:pic:findGoodsPicByGoodsSyncId";
    /**
     * 商品图文详情 - 根据商品同步Id查询商品图文详情信息
     */
    public static final String EL_SHOP_GOODS_DETAIL_getGoodsDetailByGoodsSyncId = "nds:shop:goods:detail:getGoodsDetailByGoodsSyncId";

    /**
     * 商家 - 商家信息(不附带关联信息)
     */
    public static final String E_SHOP_findShopInfo_JSON = "nds:shop:findShopInfo_JSON:";

    /**
     * 商家 - 商家是否开启了微信分帐
     */
    public static final String E_SHOP_IS_WXFZ = "nds:shop:ShopFn_WXFZ:";
    /**
     * 商家 - 总部门店
     */
    public static final String E_SHOP_TopStoreId = "nds:shop:TopStoreId:json:";

    /**
     * 商家 - 默认分公司
     */
    public static final String E_SHOP_DEFAULTBRANCHID = "nds:shop:defaultBranchId:";

    /**
     * 商家会员卡 - 开卡组件链接
     */
    public static final String EL_SHOP_CARD_OPEN_URL = "nds:shop:card:open:url:";

    /**
     * 用户-分公司推广人
     */
    public static final String USER_BRANCH_EMP = "nds:userBranchEmp:";

    public static final String PRESELL_DEFAULT_INSURE="default:insure:";


    /**
     * h5 认证默认缓存时间： 7 天
     */
    public static final long AUTH_DEFAULT_H5_TIMEOUT = 1;
    /**
     * h5 用户信息 - 验证/登录后的用户信息
     */
    public static final String EL_SHOP_BUYER_AUTH_H5 = "nds:shop:buyer:auth_h5:";
    public static final String EL_SHOP_BUYER_AUTH_H5_MOBILE = "nds:shop:buyer:auth_h5:mobile:";
}
