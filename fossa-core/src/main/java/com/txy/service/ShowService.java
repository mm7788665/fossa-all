package com.txy.service;

import com.txy.vo.ShowVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.txy.entity.Show;
import com.txy.mapper.ShowMapper;

import java.util.List;

@Service
public class ShowService {

    @Autowired
    private ShowMapper showMapper;

    public List<ShowVO> list() {
        return showMapper.selectShowList();
    }

    public List<ShowVO> listByNpc(String npcId) {
        return showMapper.selectByNpcId(npcId);
    }

    public Show getById(String id) {
        return showMapper.selectById(id);
    }

    public boolean save(Show show) {
        return showMapper.insert(show) > 0;
    }

    public boolean update(Show show) {
        return showMapper.update(show) > 0;
    }

    /** 逻辑删除 */
    public boolean delete(String id) {
        return showMapper.logicDelete(id) > 0;
    }
}
