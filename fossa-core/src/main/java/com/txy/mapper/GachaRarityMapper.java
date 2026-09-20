package com.txy.mapper;

import com.txy.entity.GachaRarity;
import java.util.List;

public interface GachaRarityMapper {
    /** 按稀有度从高到低 */
    List<GachaRarity> selectAll();
    GachaRarity selectByKey(String rarityKey);
}
