package com.txy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.txy.entity.Player;

import java.util.List;

@Mapper
public interface PlayerMapper {

    List<Player> selectAll();

    Player selectById(@Param("id") String id);

    Player selectByOpenId(@Param("openId") String openId);

    int countAll();

    int insert(Player player);

    int update(Player player);

    /** 逻辑删除 */
    int logicDelete(@Param("id") String id);
}
