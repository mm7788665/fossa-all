package com.txy.mapper;

import com.txy.entity.GachaPool;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface GachaPoolMapper {

    List<GachaPool> selectAll();
    /** 后台列表只要启用中的也可以调这个；不传 status 时返回全部未删除的 */
    List<GachaPool> selectByStatus(@Param("status") String status);
    GachaPool selectById(@Param("id") String id);
    /** 不管 deleted 状态，用于判断「这行到底存不存在」，避免重复 INSERT 撞主键 */
    GachaPool selectByIdAny(@Param("id") String id);
    int insert(GachaPool pool);
    int update(GachaPool pool);
    /** 逻辑删除 */
    int softDelete(@Param("id") String id);
}
