package com.txy.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.txy.entity.Show;
import com.txy.vo.ShowVO;

import java.util.List;

/**
 * 场次 Mapper（原生 MyBatis）
 */
@Mapper
public interface ShowMapper {

    List<Show> selectAll();

    Show selectById(@Param("id") String id);

    int countAll();

    /** 关联 NPC 的场次列表（含 npc_name / npc_icon） */
    List<ShowVO> selectShowList();

    /** 按 NPC 查场次 */
    List<ShowVO> selectByNpcId(@Param("npcId") String npcId);

    /** 在售/预售场次数 */
    int countOnSale();

    /** 总上座数 */
    int sumTaken();

    /** 总容量 */
    int sumCapacity();

    int insert(Show show);

    int update(Show show);

    /** 逻辑删除 */
    int logicDelete(@Param("id") String id);
}
