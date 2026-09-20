package com.txy.controller;

import com.txy.security.RequirePerm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.txy.dto.R;
import com.txy.service.IntimacyService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/intimacy")
public class IntimacyController {

    @Autowired
    private IntimacyService intimacyService;

    @RequirePerm("intimacy:view")
    @GetMapping("/by-npc/{npcId}")
    public R<List<Map<String, Object>>> byNpc(@PathVariable String npcId) {
        return R.ok(intimacyService.listByNpc(npcId));
    }

    @RequirePerm("intimacy:view")
    @GetMapping("/matrix/{playerId}")
    public R<Map<String, Object>> matrix(@PathVariable String playerId) {
        return R.ok(intimacyService.matrixByPlayer(playerId));
    }

    @RequirePerm("intimacy:view")
    @GetMapping("/rank")
    public R<List<Map<String, Object>>> rank() {
        return R.ok(intimacyService.totalRank());
    }

    @RequirePerm(value = "intimacy:edit", module = "intimacy", desc = "调整亲密度")
    @PutMapping("/{playerId}/{npcId}")
    public R<Boolean> update(@PathVariable String playerId,
                             @PathVariable String npcId,
                             @RequestParam Integer value) {
        return R.ok(intimacyService.saveOrUpdate(playerId, npcId, value));
    }

    /** 逻辑删除（联合键） */
    @RequirePerm(value = "intimacy:edit", module = "intimacy", desc = "删除亲密度")
    @DeleteMapping("/{playerId}/{npcId}")
    public R<Boolean> delete(@PathVariable String playerId, @PathVariable String npcId) {
        return R.ok(intimacyService.delete(playerId, npcId));
    }
}
