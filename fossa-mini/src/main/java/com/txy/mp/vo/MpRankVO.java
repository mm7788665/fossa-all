package com.txy.mp.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 小程序 · 亲密度排行视图
 *   mine      : 当前玩家对各 NPC 的亲密度 { npcId: value }
 *   players   : 全服玩家 × NPC 矩阵
 *   byNpc     : 按 NPC 维度的攻略者排行 { npcId: [ {playerId,name,tag,value} ] }
 */
@Data
public class MpRankVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Integer> mine;
    private List<PlayerRow> players;
    private Map<String, List<NpcRankRow>> byNpc;

    @Data
    public static class PlayerRow implements Serializable {
        private static final long serialVersionUID = 1L;
        private String id;
        private String name;
        private String tag;
        private String avatar;
        private Map<String, Integer> npc;
        private Integer total;
    }

    @Data
    public static class NpcRankRow implements Serializable {
        private static final long serialVersionUID = 1L;
        private String playerId;
        private String playerName;
        private String playerTag;
        private String avatar;
        private Integer value;
    }
}
