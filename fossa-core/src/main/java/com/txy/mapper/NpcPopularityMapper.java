package com.txy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.txy.entity.NpcPopularity;

import java.util.List;

@Mapper
public interface NpcPopularityMapper {

    List<NpcPopularity> selectAll();

    NpcPopularity selectByNpcId(@Param("npcId") String npcId);

    int insert(NpcPopularity popularity);

    int update(NpcPopularity popularity);

    /** 逻辑删除 */
    int logicDelete(@Param("npcId") String npcId);
}
