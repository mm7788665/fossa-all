package com.txy.controller;

import com.txy.security.RequirePerm;
import com.txy.service.ShowService;
import com.txy.vo.ShowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.txy.dto.R;
import com.txy.entity.Show;


import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @RequirePerm("show:view")
    @GetMapping
    public R<List<ShowVO>> list(@RequestParam(required = false) String npcId) {
        if (npcId != null) return R.ok(showService.listByNpc(npcId));
        return R.ok(showService.list());
    }

    @RequirePerm("show:view")
    @GetMapping("/{id}")
    public R<Show> get(@PathVariable String id) {
        return R.ok(showService.getById(id));
    }

    @RequirePerm(value = "show:edit", module = "show", desc = "新增场次")
    @PostMapping
    public R<Boolean> create(@RequestBody Show show) {
        return R.ok(showService.save(show));
    }

    @RequirePerm(value = "show:edit", module = "show", desc = "编辑场次")
    @PutMapping("/{id}")
    public R<Boolean> update(@PathVariable String id, @RequestBody Show show) {
        show.setId(id);
        return R.ok(showService.update(show));
    }

    @RequirePerm(value = "show:delete", module = "show", desc = "删除场次")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable String id) {
        return R.ok(showService.delete(id));
    }
}
