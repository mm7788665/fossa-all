package com.txy.mp.vo;

import lombok.Data;
import java.io.Serializable;

/** 小程序 · 公告视图（只下发已发布） */
@Data
public class MpNoticeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String cat;
    private Integer top;
    private String content;
    private String author;
    private String createdAt;
}
