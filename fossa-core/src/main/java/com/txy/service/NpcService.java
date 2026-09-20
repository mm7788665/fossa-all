package com.txy.service;

import com.txy.entity.Npc;
import com.txy.mapper.NpcMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;

@Service
public class NpcService {

    @Autowired
    private NpcMapper npcMapper;

    public List<Npc> list(String keyword, String faction,String status) {
        return npcMapper.selectAll(keyword,faction,status);
    }

    public Npc getById(String id) {
        return npcMapper.selectById(id);
    }

    public boolean save(Npc npc) {
        npc.setId(UUID.randomUUID().toString());
        return npcMapper.insert(npc) > 0;
    }

    public boolean update(Npc npc) {
        return npcMapper.update(npc) > 0;
    }

    /** 逻辑删除：deleted = 1，数据保留 */
    public boolean delete(String id) {
        return npcMapper.logicDelete(id) > 0;
    }
}
