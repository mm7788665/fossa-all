package com.txy.controller;

import com.txy.dto.R;
import com.txy.entity.AdminAuditLog;
import com.txy.mapper.AdminAuditMapper;
import com.txy.security.RequirePerm;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志查询
 * 完整路径 /fossa/api/audit/**
 */
@RestController
@RequestMapping("/api/audit")
public class AdminAuditController {

    private final AdminAuditMapper auditMapper;

    public AdminAuditController(AdminAuditMapper auditMapper){ this.auditMapper = auditMapper; }

    @RequirePerm(value = "audit:view", module = "audit", desc = "查看操作日志")
    @GetMapping
    public R<Map<String, Object>> list(
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Integer success,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size){
        if (page < 1) page = 1;
        if (size < 1) size = 20;
        if (size > 200) size = 200;
        int offset = (page - 1) * size;
        List<AdminAuditLog> rows = auditMapper.select(user, module, action, success, kw, dateFrom, dateTo, offset, size);
        int total = auditMapper.count(user, module, action, success, kw, dateFrom, dateTo);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("rows", rows);
        m.put("total", total);
        m.put("page", page);
        m.put("size", size);
        return R.ok(m);
    }

    /** 清理 N 天前的日志 */
    @RequirePerm(value = "audit:view", module = "audit", desc = "清理历史日志")
    @DeleteMapping("/clean")
    public R<Integer> clean(@RequestParam(defaultValue = "90") int days){
        if (days < 7) return R.fail(400,"至少保留 7 天");
        return R.ok(auditMapper.deleteBefore(days));
    }
}
