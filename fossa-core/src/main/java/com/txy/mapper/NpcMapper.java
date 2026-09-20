package com.txy.mapper;

import com.txy.entity.Npc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * NPC Mapper（原生 MyBatis，SQL 见 resources/mapper/NpcMapper.xml）
 * 所有查询自动过滤 deleted = 0；删除走 logicDelete（UPDATE SET deleted=1）
 */
@Mapper
public interface NpcMapper {

    /** 查询全部未删除 */
    List<Npc> selectAll(@Param("words") String words,@Param("faction") String faction,@Param("status") String status);

    /** 按 id 查询未删除 */
    Npc selectById(@Param("id") String id);

    /** 统计未删除数量 */
    int countAll();

    /** 新增 */
    int insert(Npc npc);

    /** 更新 */
    int update(Npc npc);

    /** 逻辑删除（deleted = 1） */
    int logicDelete(@Param("id") String id);
}
