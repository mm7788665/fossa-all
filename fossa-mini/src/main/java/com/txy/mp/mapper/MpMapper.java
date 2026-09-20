package com.txy.mp.mapper;

import com.txy.fossa.web.entity.Player;
import com.txy.fossa.web.mp.vo.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 小程序接口 Mapper —— 只读玩法数据 + 少量写入
 * 全部 SQL 见 resources/mapper/MpMapper.xml
 */
public interface MpMapper {

    // ---------- NPC ----------
    List<MpNpcVO> selectNpcList();

    MpNpcVO selectNpcById(@Param("id") String id);

    // ---------- 公告 ----------
    List<MpNoticeVO> selectNoticeList();

    // ---------- 场次 / 预约 ----------
    List<MpShowVO> selectShowList();

    MpShowVO selectShowById(@Param("id") String id);

    int incrShowTaken(@Param("id") String id, @Param("qty") int qty);

    // ---------- 抽卡 ----------
    List<MpGachaPrizeVO> selectGachaPool(@Param("group") String group);

    int insertGachaRecord(@Param("id") String id, @Param("playerId") String playerId,
                          @Param("poolGroup") String poolGroup, @Param("prizeId") String prizeId,
                          @Param("prizeName") String prizeName, @Param("rarity") int rarity);

    // ---------- 玩家 / 亲密度 ----------
    Player selectPlayerById(@Param("id") String id);

    List<Map<String, Object>> selectIntimacyOf(@Param("playerId") String playerId);

    /** 全服亲密度明细：player_id, player_name, player_tag, avatar, npc_id, value */
    List<Map<String, Object>> selectIntimacyAll();

    int upsertIntimacy(@Param("playerId") String playerId, @Param("npcId") String npcId,
                       @Param("value") int value);

    // ---------- 订单 ----------
    List<MpOrderVO> selectOrdersOf(@Param("playerId") String playerId);

    int insertOrder(@Param("id") String id, @Param("playerId") String playerId,
                    @Param("showId") String showId, @Param("userName") String userName,
                    @Param("amount") java.math.BigDecimal amount,
                    @Param("qty") int qty, @Param("status") String status);
}
