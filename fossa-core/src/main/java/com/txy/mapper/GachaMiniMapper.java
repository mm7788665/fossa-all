package com.txy.mapper;

import com.txy.entity.*;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Set;

/** 小程序抽卡 Mapper（只读后台的 pool/card/rarity 三表，读写本模块的 record/pity/card 三表） */
public interface GachaMiniMapper {

    /* ---- 后台配置表：只读 ---- */
    List<GachaPool> selectOpenPools();
    GachaPool selectOpenPoolById(@Param("id") String id);
    List<GachaCard> selectCards(@Param("poolId") String poolId);
    int countCards(@Param("poolId") String poolId);
    List<GachaRarity> selectRarity();

    /* ---- 保底进度 ---- */
    GachaUserPity selectPity(@Param("userId") String userId, @Param("poolId") String poolId);
    void insertPity(GachaUserPity pity);
    void updatePity(GachaUserPity pity);

    /* ---- 抽卡记录 ---- */
    void batchInsertRecords(@Param("list") List<GachaDrawRecord> list);
    List<GachaDrawRecord> selectRecords(@Param("userId") String userId,
                                        @Param("poolId") String poolId,
                                        @Param("limit") int limit);

    /* ---- 用户卡牌 ---- */
    Set<String> selectOwnedCardIds(@Param("userId") String userId);
    List<GachaUserCard> selectUserCards(@Param("userId") String userId, @Param("rarity") String rarity);
    GachaUserCard selectUserCard(@Param("userId") String userId, @Param("cardId") String cardId);
    void insertUserCard(GachaUserCard card);
    void incrUserCard(@Param("userId") String userId, @Param("cardId") String cardId);

    /* ---- 库存 ---- */
    void decrStock(@Param("cardId") String cardId);
}
