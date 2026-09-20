package com.txy.controller;


import com.txy.common.R;
import com.txy.entity.Npc;
import com.txy.fossa.web.service.NpcService;
import com.txy.fossa.web.vo.PageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * NPC 角色密档接口
 *
 * GET    /api/npc             分页 + 条件查询（keyword / faction / status / pageNum / pageSize）
 * GET    /api/npc/{id}        详情
 * GET    /api/npc/simple      下拉列表（启用状态）
 * POST   /api/npc             新增
 * PUT    /api/npc/{id}        更新
 * DELETE /api/npc/{id}        删除
 * PATCH  /api/npc/{id}/status 切换启用/禁用  body: { "status": "启用|禁用" }
 * PATCH  /api/npc/{id}/sort   修改排序      body: { "sort": 1 }
 */
@RestController
@RequestMapping("/api/npc")
public class NpcController {

    @Autowired
    private NpcService npcService;

    @GetMapping
    public R<PageData<Npc>> page(@Valid NpcQuery query) {
        return npcService.page(query);
    }

    @GetMapping("/simple")
    public R<?> simple() {
        return npcService.simple();
    }

    @GetMapping("/{id}")
    public R<Npc> detail(@PathVariable String id) {
        return npcService.detail(id);
    }

    @PostMapping
    public R<Npc> create(@RequestBody Npc npc) {
        return npcService.create(npc);
    }

    @PutMapping("/{id}")
    public R<Npc> update(@PathVariable String id, @RequestBody Npc npc) {
        return npcService.update(id, npc);
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable String id) {
        return npcService.delete(id);
    }

    @PatchMapping("/{id}/status")
    public R<Npc> toggleStatus(@PathVariable String id, @RequestBody StatusReq req) {
        return npcService.toggleStatus(id, req.getStatus());
    }

    @PatchMapping("/{id}/sort")
    public R<Npc> updateSort(@PathVariable String id, @RequestBody SortReq req) {
        return npcService.updateSort(id, req.getSort());
    }

    /** 内部 DTO：切换状态 */
    public static class StatusReq {
        private String status;
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /** 内部 DTO：排序 */
    public static class SortReq {
        private Integer sort;
        public Integer getSort() { return sort; }
        public void setSort(Integer sort) { this.sort = sort; }
    }
}
