package com.txy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.txy.entity.Intimacy;
import com.txy.mapper.IntimacyMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IntimacyService {

    @Autowired
    private IntimacyMapper intimacyMapper;

    public List<Map<String, Object>> listByNpc(String npcId) {
        return intimacyMapper.selectByNpc(npcId);
    }

    /** 玩家亲密度矩阵：{ playerId, npc: { linshen: 45, ... } } */
    public Map<String, Object> matrixByPlayer(String playerId) {
        List<Map<String, Object>> rows = intimacyMapper.selectByPlayer(playerId);
        Map<String, Object> npcMap = new HashMap<String, Object>();
        for (Map<String, Object> r : rows) {
            npcMap.put(String.valueOf(r.get("npcId")), r.get("value"));
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("playerId", playerId);
        result.put("npc", npcMap);
        return result;
    }

    public List<Map<String, Object>> totalRank() {
        return intimacyMapper.selectTotalRank();
    }

    /** 新增或更新（已存在则改 value，不存在则 insert） */
    public boolean saveOrUpdate(String playerId, String npcId, Integer value) {
        Intimacy exist = intimacyMapper.selectOne(playerId, npcId);
        Intimacy e = new Intimacy();
        e.setPlayerId(playerId);
        e.setNpcId(npcId);
        e.setValue(value);
        if (exist == null) {
            return intimacyMapper.insert(e) > 0;
        }
        return intimacyMapper.update(e) > 0;
    }

    /** 逻辑删除（联合键） */
    public boolean delete(String playerId, String npcId) {
        return intimacyMapper.logicDelete(playerId, npcId) > 0;
    }

    /** 亲密度等级文案（与前端 DB.intimacyLevel 阈值一致） */
    public static String levelOf(Integer value) {
        int v = (value == null) ? 0 : value;
        if (v >= 80) return "Lv.4 不可分割";
        if (v >= 60) return "Lv.3 心跳共振";
        if (v >= 40) return "Lv.2 暗室同行";
        return "Lv.1 萍水相逢";
    }
}
