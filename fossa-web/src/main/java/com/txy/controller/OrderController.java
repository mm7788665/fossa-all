package com.txy.controller;

import com.txy.security.RequirePerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.txy.dto.R;
import com.txy.service.OrderService;
import com.txy.vo.OrderVO;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @RequirePerm("order:view")
    @GetMapping
    public R<List<OrderVO>> list() {
        return R.ok(orderService.list());
    }

    @RequirePerm(value = "order:edit", module = "order", desc = "改订单状态")
    @PutMapping("/{id}/status")
    public R<Boolean> updateStatus(@PathVariable String id, @RequestParam String status) {
        return R.ok(orderService.updateStatus(id, status));
    }

    /** 逻辑删除 */
    @RequirePerm(value = "order:delete", module = "order", desc = "删除订单")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable String id) {
        return R.ok(orderService.delete(id));
    }

    @RequirePerm("order:view")
    @GetMapping("/status-options")
    public R<List<String>> statusOptions() {
        return R.ok(orderService.statusOptions());
    }
}
