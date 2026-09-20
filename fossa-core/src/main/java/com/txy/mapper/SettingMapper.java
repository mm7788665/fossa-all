package com.txy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.txy.entity.Setting;

import java.util.List;

@Mapper
public interface SettingMapper {

    List<Setting> selectAll();

    Setting selectByKey(@Param("k") String k);

    int insert(Setting setting);

    int update(Setting setting);

    /** 逻辑删除（按 k） */
    int logicDelete(@Param("k") String k);
}
