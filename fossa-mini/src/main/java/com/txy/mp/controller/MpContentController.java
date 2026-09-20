package com.txy.mp.controller;

import com.txy.fossa.web.common.R;
import com.txy.fossa.web.mp.service.MpService;
import com.txy.fossa.web.mp.vo.MpNpcVO;
import com.txy.fossa.web.mp.vo.MpNoticeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 小程序 · 内容接口（游客可读，未登录也能访问）
 *
 * GET /api/mp/npc/list          角色列表（启用 + 按 sort）
 * GET /api/mp/npc/detail?id=    角色详情（含四钩子）
 * GET /api/mp/notice/list       公告列表（已发布 + 置顶优先）
 */
@RestController
@RequestMapping("/api/mp")
public class MpContentController extends MpBaseController {

    @Autowired
    private MpService mpService;

    @GetMapping("/npc/list")
    public R<List<MpNpcVO>> npcList() {
        return mpService.npcList();
    }

    @GetMapping("/npc/detail")
    public R<MpNpcVO> npcDetail(@RequestParam("id") String id) {
        return mpService.npcDetail(id);
    }

    @GetMapping("/notice/list")
    public R<List<MpNoticeVO>> noticeList() {
        return mpService.noticeList();
    }
}
