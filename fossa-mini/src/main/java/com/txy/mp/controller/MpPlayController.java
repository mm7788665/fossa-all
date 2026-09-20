package com.txy.mp.controller;

import com.txy.fossa.web.common.R;
import com.txy.fossa.web.mp.service.MpService;
import com.txy.fossa.web.mp.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 小程序 · 玩法接口
 *
 * GET  /api/mp/show/list              场次列表（游客可读）
 * POST /api/mp/order/create           预约下单（需登录） body:{showId, qty, userName}
 * GET  /api/mp/order/list             我的订单（需登录）
 * GET  /api/mp/gacha/pool             奖池（游客可读）
 * POST /api/mp/gacha/draw             抽卡（需登录，服务端算概率）body:{group, count}
 */
@RestController
@RequestMapping("/api/mp")
public class MpPlayController extends MpBaseController {

    @Autowired
    private MpService mpService;

    @GetMapping("/show/list")
    public R<List<MpShowVO>> showList() {
        return mpService.showList();
    }

    @PostMapping("/order/create")
    public R<Map<String, Object>> orderCreate(@RequestBody Map<String, Object> body,
                                              HttpServletRequest request) {
        String pid = requirePlayerId(request);
        String showId = body == null ? null : (String) body.get("showId");
        Integer qty = body == null ? null : toInt(body.get("qty"));
        String userName = body == null ? null : (String) body.get("userName");
        return mpService.book(pid, showId, qty, userName);
    }

    @GetMapping("/order/list")
    public R<List<MpOrderVO>> orderList(HttpServletRequest request) {
        return mpService.myOrders(requirePlayerId(request));
    }

    @GetMapping("/gacha/pool")
    public R<Map<String, List<MpGachaPrizeVO>>> gachaPool() {
        return mpService.gachaPool();
    }

    @PostMapping("/gacha/draw")
    public R<List<MpGachaPrizeVO>> gachaDraw(@RequestBody Map<String, Object> body,
                                             HttpServletRequest request) {
        String pid = requirePlayerId(request);
        String group = body == null ? null : (String) body.get("group");
        Integer count = body == null ? null : toInt(body.get("count"));
        return mpService.gachaDraw(pid, group, count);
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.valueOf(String.valueOf(o)); } catch (Exception e) { return null; }
    }
}
