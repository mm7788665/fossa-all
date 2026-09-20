package com.txy.controller;

import com.txy.security.RequirePerm;
import com.txy.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.txy.dto.R;
import com.txy.entity.Notice;

import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @RequirePerm("notice:view")
    @GetMapping
    public R<List<Notice>> list() {
        return R.ok(noticeService.list());
    }

    @RequirePerm("notice:view")
    @GetMapping("/{id}")
    public R<Notice> get(@PathVariable String id) {
        return R.ok(noticeService.getById(id));
    }

    @RequirePerm(value = "notice:edit", module = "notice", desc = "新增公告")
    @PostMapping
    public R<Boolean> create(@RequestBody Notice notice) {
        return R.ok(noticeService.save(notice));
    }

    @RequirePerm(value = "notice:edit", module = "notice", desc = "编辑公告")
    @PutMapping("/{id}")
    public R<Boolean> update(@PathVariable String id, @RequestBody Notice notice) {
        notice.setId(id);
        return R.ok(noticeService.update(notice));
    }

    @RequirePerm(value = "notice:delete", module = "notice", desc = "删除公告")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable String id) {
        return R.ok(noticeService.delete(id));
    }
}
