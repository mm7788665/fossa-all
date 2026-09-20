package com.txy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.txy.entity.PlayerActivity;

import java.util.List;

@Mapper
public interface PlayerActivityMapper {

    List<PlayerActivity> selectAll();

    PlayerActivity selectByPlayerId(@Param("playerId") String playerId);

    int insert(PlayerActivity activity);

    int update(PlayerActivity activity);

    /** 逻辑删除 */
    int logicDelete(@Param("playerId") String playerId);
}
