package com.txy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.txy.entity.Intimacy;

import java.util.List;
import java.util.Map;

@Mapper
public interface IntimacyMapper {

    List<Intimacy> selectAll();

    /** 按 NPC 查（含 npc_name / player_name） */
    List<Map<String, Object>> selectByNpc(@Param("npcId") String npcId);

    /** 某玩家的全部 NPC 亲密度 */
    List<Map<String, Object>> selectByPlayer(@Param("playerId") String playerId);

    /** 亲密度总榜（按 total 降序） */
    List<Map<String, Object>> selectTotalRank();

    /** 查单条（用于 upsert 判断） */
    Intimacy selectOne(@Param("playerId") String playerId, @Param("npcId") String npcId);

    int insert(Intimacy intimacy);

    int update(Intimacy intimacy);

    /** 逻辑删除（联合键） */
    int logicDelete(@Param("playerId") String playerId, @Param("npcId") String npcId);
}
