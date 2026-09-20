package com.txy.controller;

import com.txy.dto.R;
import com.txy.entity.AdminPerm;
import com.txy.entity.AdminRole;
import com.txy.mapper.AdminRoleMapper;
import com.txy.security.RequirePerm;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色与权限管理
 * 完整路径 /fossa/api/roles/**
 */
@RestController
@RequestMapping("/api/roles")
public class AdminRoleController {

    private final AdminRoleMapper roleMapper;

    public AdminRoleController(AdminRoleMapper roleMapper){ this.roleMapper = roleMapper; }

    /** 角色列表（含各自的权限码） */
    @RequirePerm(value = "role:view", module = "role", desc = "查看角色")
    @GetMapping
    public R<List<AdminRole>> list(){
        return R.ok(roleMapper.selectRoles());
    }

    /** 全部权限点（后台勾选框用） */
    @RequirePerm(value = "role:view", module = "role")
    @GetMapping("/perms")
    public R<List<AdminPerm>> perms(){
        return R.ok(roleMapper.selectPerms());
    }

    /** 权限点按模块分组（后台渲染更方便） */
    @RequirePerm(value = "role:view", module = "role")
    @GetMapping("/perms/grouped")
    public R<List<Map<String, Object>>> permsGrouped(){
        List<AdminPerm> all = roleMapper.selectPerms();
        Map<String, List<AdminPerm>> g = new LinkedHashMap<>();
        for (AdminPerm p : all){
            g.computeIfAbsent(p.getModule(), k -> new ArrayList<>()).add(p);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, List<AdminPerm>> e : g.entrySet()){
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("module", e.getKey());
            m.put("moduleName", MODULE_NAME.getOrDefault(e.getKey(), e.getKey()));
            m.put("items", e.getValue());
            out.add(m);
        }
        return R.ok(out);
    }

    /** 新建角色 */
    @RequirePerm(value = "role:edit", module = "role", desc = "新建角色")
    @PostMapping
    public R<Boolean> create(@RequestBody AdminRole r){
        if (r == null || r.getRoleKey() == null || r.getRoleKey().trim().isEmpty()){
            return R.fail(400,"角色标识不能为空");
        }
        if (roleMapper.selectRole(r.getRoleKey().trim()) != null) return R.fail(400,"该角色标识已存在");
        r.setRoleKey(r.getRoleKey().trim());
        roleMapper.insertRole(r);
        savePerms(r.getRoleKey(), r.getPerms());
        return R.ok(true);
    }

    /** 更新角色（含权限全量替换） */
    @RequirePerm(value = "role:edit", module = "role", desc = "修改角色权限")
    @PutMapping("/{roleKey}")
    public R<Boolean> update(@PathVariable("roleKey") String roleKey, @RequestBody AdminRole r){
        if (r == null) return R.fail(400,"参数为空");
        AdminRole exist = roleMapper.selectRole(roleKey);
        if (exist == null) return R.fail(400,"角色不存在");
        if (exist.getBuiltin() == 1 && !roleKey.equals(r.getRoleKey())){
            return R.fail(400,"内置角色不可改名");
        }
        r.setRoleKey(roleKey);
        roleMapper.updateRole(r);
        savePerms(roleKey, r.getPerms());
        return R.ok(true);
    }

    /** 删除角色（内置角色、有账号在用的不能删） */
    @RequirePerm(value = "role:edit", module = "role", desc = "删除角色")
    @DeleteMapping("/{roleKey}")
    public R<Boolean> delete(@PathVariable("roleKey") String roleKey){
        AdminRole exist = roleMapper.selectRole(roleKey);
        if (exist == null) return R.fail(400,"角色不存在");
        if (exist.getBuiltin() == 1) return R.fail(400,"内置角色不可删除");
        if ("super".equals(roleKey)) return R.fail(400,"超级管理员不可删除");
        int used = roleMapper.countAccountByRole(roleKey);
        if (used > 0) return R.fail(400,"还有 " + used + " 个账号在使用该角色，请先改归属");
        roleMapper.deletePerms(roleKey);
        roleMapper.softDeleteRole(roleKey);
        return R.ok(true);
    }

    private void savePerms(String roleKey, List<String> perms){
        roleMapper.deletePerms(roleKey);
        if (perms != null && !perms.isEmpty()){
            /* 过滤掉不存在的权限码，防止脏数据 */
            List<String> valid = new ArrayList<>();
            List<AdminPerm> all = roleMapper.selectPerms();
            for (String p : perms){
                if ("*".equals(p)){ valid.add(p); continue; }
                for (AdminPerm ap : all) if (ap.getPermCode().equals(p)){ valid.add(p); break; }
            }
            if (!valid.isEmpty()) roleMapper.insertPerms(roleKey, valid);
        }
    }

    private static final Map<String,String> MODULE_NAME = new LinkedHashMap<>();
    static {
        MODULE_NAME.put("dashboard","控制台");
        MODULE_NAME.put("npc","角色管理");
        MODULE_NAME.put("show","场次管理");
        MODULE_NAME.put("order","订单管理");
        MODULE_NAME.put("notice","公告管理");
        MODULE_NAME.put("player","玩家管理");
        MODULE_NAME.put("intimacy","亲密度");
        MODULE_NAME.put("gacha","抽卡奖池");
        MODULE_NAME.put("setting","系统设置");
        MODULE_NAME.put("account","账号管理");
        MODULE_NAME.put("role","角色权限");
        MODULE_NAME.put("audit","操作日志");
        MODULE_NAME.put("upload","图片上传");
    }
}
