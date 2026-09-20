package com.txy.mp.controller;

import com.txy.fossa.web.common.R;
import com.txy.fossa.web.mp.service.MpService;
import com.txy.fossa.web.mp.vo.MpPlayerVO;
import com.txy.fossa.web.mp.vo.MpRankVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 小程序 · 玩家接口
 *
 * GET /api/mp/player/me    我的信息 + 我的亲密度（需登录）
 * GET /api/mp/rank/intimacy 亲密度排行（游客可读，未登录时 mine 为空）
 */
@RestController
@RequestMapping("/api/mp")
public class MpPlayerController extends MpBaseController {

    @Autowired
    private MpService mpService;

    @GetMapping("/player/me")
    public R<MpPlayerVO> me(HttpServletRequest request) {
        return mpService.playerMe(requirePlayerId(request));
    }

    @GetMapping("/rank/intimacy")
    public R<MpRankVO> rank(HttpServletRequest request) {
        String pid = playerId(request);
        return mpService.rank(pid == null ? "" : pid);
    }
}
