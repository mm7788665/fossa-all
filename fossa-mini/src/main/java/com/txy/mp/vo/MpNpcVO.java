package com.txy.mp.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 小程序 · NPC 卡片/详情视图
 * 字段命名与小程序 data/npcs.js 保持一致（hooks 为四钩子对象）
 */
@Data
public class MpNpcVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String icon;
    private String cover;
    private String role;
    private String faction;
    private String campLabel;
    private String power;
    private String ability;
    private String cost;
    private List<String> tags;
    private String desc;
    private List<String> quotes;
    private Hooks hooks;

    @Data
    public static class Hooks implements Serializable {
        private static final long serialVersionUID = 1L;
        private String action;
        private String contrast;
        private String fragile;
        private String speech;
    }
}
