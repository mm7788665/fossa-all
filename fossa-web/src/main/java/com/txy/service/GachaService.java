package com.txy.service;

import com.txy.entity.GachaPool;
import java.util.List;
import java.util.Map;

public interface GachaService {

    /** 奖池列表（含 cards） */
    List<GachaPool> listWithCards();

    /** 只返回启用中的（fossa-mini 会用到） */
    List<GachaPool> listOpenWithCards();

    /** 单个奖池（含 cards） */
    GachaPool getWithCards(String id);

    /** 保存奖池基本信息 */
    GachaPool save(GachaPool pool);

    /** 整池提交：规则 + 保底 + 卡牌一起保存 */
    boolean saveWithCards(GachaPool pool);

    /** 删除奖池及其卡牌 */
    boolean removeWithCards(String id);

    /** 只改某张卡的权重 */
    boolean updateWeight(String cardId, int weight);

    /** 稀有度概率表，返回 { SSR:2, SR:12, R:36, N:50 } */
    Map<String, Object> rarityRateMap();

    /** 稀有度完整定义（给需要展示名/颜色的地方用） */
    List<com.txy.entity.GachaRarity> rarityList();
}
