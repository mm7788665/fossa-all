package com.txy.controller;

import com.txy.security.RequirePerm;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.txy.dto.R;
import com.txy.entity.Player;
import com.txy.service.PlayerService;

import java.util.List;

/**
 * 玩家管理：目前后端只提供查询，没有新增/编辑/删除接口。
 * 所以只加 player:view；将来补了写接口，照下面的注释加注解即可。
 */
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @RequirePerm("player:view")
    @GetMapping
    public R<List<Player>> list() {
        return R.ok(playerService.list());
    }

    @RequirePerm("player:view")
    @GetMapping("/{id}")
    public R<Player> get(@PathVariable String id) {
        return R.ok(playerService.getById(id));
    }

    /* 将来补写接口时，按这个模板加：
    @RequirePerm(value = "player:edit", module = "player", desc = "编辑玩家")
    @PutMapping("/{id}")
    public R<Boolean> update(@PathVariable String id, @RequestBody Player player) { ... }

    @RequirePerm(value = "player:delete", module = "player", desc = "删除玩家")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable String id) { ... }
    */
}
