package com.txy.controller;

import com.txy.security.RequirePerm;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.txy.dto.R;
import com.txy.entity.Npc;
import com.txy.service.NpcService;

import java.util.List;

@RestController
@RequestMapping("/api/npcs")
@RequiredArgsConstructor
public class NpcController {

    private final NpcService npcService;

    @RequirePerm("npc:view")
    @GetMapping
    public R<List<Npc>> list(@RequestParam(required = false, defaultValue = "") String keyword,
                             @RequestParam(required = false, defaultValue = "") String faction,
                             @RequestParam(required = false) String status) {
        return R.ok(npcService.list(keyword, faction, status));
    }

    @RequirePerm("npc:view")
    @GetMapping("/{id}")
    public R<Npc> get(@PathVariable String id) {
        return R.ok(npcService.getById(id));
    }

    @RequirePerm(value = "npc:edit", module = "npc", desc = "新增角色")
    @PostMapping
    public R<Boolean> create(@RequestBody Npc npc) {
        return R.ok(npcService.save(npc));
    }

    @RequirePerm(value = "npc:edit", module = "npc", desc = "编辑角色")
    @PutMapping("/{id}")
    public R<Boolean> update(@PathVariable String id, @RequestBody Npc npc) {
        npc.setId(id);
        return R.ok(npcService.update(npc));
    }

    @RequirePerm(value = "npc:delete", module = "npc", desc = "删除角色")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable String id) {
        return R.ok(npcService.delete(id));
    }
}
