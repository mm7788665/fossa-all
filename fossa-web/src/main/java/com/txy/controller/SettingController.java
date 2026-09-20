package com.txy.controller;

import com.txy.security.RequirePerm;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.txy.dto.R;
import com.txy.service.SettingService;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @RequirePerm("setting:view")
    @GetMapping
    public R<Map<String, String>> all() {
        return R.ok(settingService.all());
    }

    @RequirePerm(value = "setting:edit", module = "setting", desc = "修改系统设置")
    @PutMapping
    public R<Boolean> update(@RequestBody Map<String, String> kv) {
        return R.ok(settingService.update(kv));
    }
}
