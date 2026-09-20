package com.txy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.txy.entity.Notice;

import java.util.List;

@Mapper
public interface NoticeMapper {

    List<Notice> selectAll();

    Notice selectById(@Param("id") String id);

    int countAll();

    /** 统计状态为发布的公告数 */
    int countPublished();

    int insert(Notice notice);

    int update(Notice notice);

    /** 逻辑删除 */
    int logicDelete(@Param("id") String id);
}
