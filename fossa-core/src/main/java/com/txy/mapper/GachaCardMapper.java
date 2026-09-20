package com.txy.mapper;

import com.txy.entity.GachaCard;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface GachaCardMapper {

    List<GachaCard> selectByPool(@Param("poolId") String poolId);
    GachaCard selectById(@Param("id") String id);
    int insert(GachaCard card);
    int update(GachaCard card);
    /** 只改权重：滑块拖动高频调用，比整卡更新轻 */
    int updateWeight(@Param("id") String id, @Param("weight") int weight);
    /** 逻辑删除单卡 */
    int softDelete(@Param("id") String id);
    /** 删除整个奖池下的卡（随奖池一起删） */
    int softDeleteByPool(@Param("poolId") String poolId);

    /**
     * 存在则更新（并把 deleted 置 0），不存在则插入。
     * 整池保存必须走这个，不能先软删再 INSERT ——
     * 软删只是把 deleted 置 1，行还在，再 INSERT 同 id 会撞主键。
     */
    int upsert(GachaCard card);

    /** 把「不在本次提交列表里」的卡软删（差量删除，保留未改动的行） */
    int softDeleteExcept(@Param("poolId") String poolId, @Param("ids") List<String> ids);
}
