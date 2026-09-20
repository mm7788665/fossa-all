package com.txy.controller;

import com.txy.dto.R;
import com.txy.entity.AdminAccount;
import com.txy.entity.AdminSession;
import com.txy.mapper.AdminAuthMapper;
import com.txy.security.AdminContext;
import com.txy.security.PasswordKit;
import com.txy.security.PublicApi;
import com.txy.security.RequirePerm;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 登录 / 登出 / 当前用户 / 修改密码
 *
 * 完整路径 /fossa/api/auth/**
 * 兼容旧路径 /fossa/api/login（前端最早对接的那个）
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AdminAuthMapper authMapper;

    public AuthController(AdminAuthMapper authMapper){ this.authMapper = authMapper; }

    /* ---------------- 登录 ---------------- */

    public static class LoginRequest {
        private String user;
        private String pass;
        public String getUser(){ return user; }
        public void setUser(String v){ this.user = v; }
        public String getPass(){ return pass; }
        public void setPass(String v){ this.pass = v; }
    }

    public static class LoginResult {
        private String token;
        private String user;
        private String name;
        private String role;
        private String roleName;
        private List<String> perms;
        private long expireAt;
        private int mustChangePwd;   // 1=仍在使用弱密码，建议改
        public LoginResult(String token, String user, String name, String role,
                           String roleName, List<String> perms, long expireAt, int mustChangePwd){
            this.token = token; this.user = user; this.name = name; this.role = role;
            this.roleName = roleName; this.perms = perms; this.expireAt = expireAt;
            this.mustChangePwd = mustChangePwd;
        }
        public String getToken(){ return token; }
        public String getUser(){ return user; }
        public String getName(){ return name; }
        public String getRole(){ return role; }
        public String getRoleName(){ return roleName; }
        public List<String> getPerms(){ return perms; }
        public long getExpireAt(){ return expireAt; }
        public int getMustChangePwd(){ return mustChangePwd; }
    }

    /** 登录：兼容 /fossa/api/login 与 /fossa/api/auth/login */
    @PublicApi
    @PostMapping({"/login", "/auth/login"})
    public R<LoginResult> login(@RequestBody LoginRequest req, HttpServletRequest http){
        if (req == null || req.getUser() == null || req.getUser().trim().isEmpty()){
            return R.fail(400,"请输入账号");
        }
        AdminAccount acc = authMapper.selectByUser(req.getUser().trim());
        if (acc == null) return R.fail(400,"账号或密码错误");

        /* ★ 密码校验：BCrypt 优先，兼容存量明文 */
        String input = req.getPass() == null ? "" : req.getPass();
        if (!PasswordKit.matches(input, acc.getPass())) return R.fail(400,"账号或密码错误");

        /* ★ 平滑迁移：库里还是明文的话，校验通过后顺手升级成 BCrypt，用户无感知 */
        boolean wasPlain = !PasswordKit.isBcrypt(acc.getPass());
        if (wasPlain){
            try{ authMapper.updatePass(acc.getUser(), PasswordKit.encode(input)); }
            catch(Exception ignore){ /* 升级失败不影响本次登录，下次还会再试 */ }
        }

        /* 生成会话 */
        String token = UUID.randomUUID().toString().replace("-", "")
                     + UUID.randomUUID().toString().replace("-", "");
        token = token.substring(0, 64);
        LocalDateTime expire = LocalDateTime.now().plusHours(8);

        AdminSession s = new AdminSession();
        s.setToken(token);
        s.setUser(acc.getUser());
        s.setName(acc.getName());
        s.setRoleKey(acc.getRole());
        s.setIp(clientIp(http));
        s.setUa(http.getHeader("User-Agent"));
        s.setExpireAt(expire);
        authMapper.insertSession(s);
        authMapper.updateLastLogin(acc.getUser());
        authMapper.deleteExpiredSessions();

        /* ★ role 归一化：没跑迁移 SQL 时库里还是中文（超级管理员/运营编辑），
         *   直接查 admin_role_perm 会查不到任何权限 → 登录成功但一个页面都进不去 */
        String roleKey = normalizeRole(acc.getRole());
        List<String> perms = authMapper.selectPermsByRole(roleKey);
        if (perms == null) perms = new ArrayList<>();
        if ("super".equals(roleKey)) perms.add("*");
        /* 保底：万一权限表没数据，超级管理员至少不被锁死在门外 */
        if (perms.isEmpty() && "super".equals(roleKey)) perms.add("*");

        /* 弱密码提醒：用了默认密码 123456 就提示改（不强制，只是前端弹个提示） */
        int mustChange = ("123456".equals(input) || input.length() < 6) ? 1 : 0;

        return R.ok(new LoginResult(token, acc.getUser(), acc.getName(), acc.getRole(),
                roleName(acc.getRole()), perms,
                expire.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
                mustChange));
    }

    /* ---------------- 登出 ---------------- */

    @PostMapping({"/logout", "/auth/logout"})
    public R<Boolean> logout(){
        String token = AdminContext.getToken();
        if (token != null) authMapper.deleteSession(token);
        return R.ok(true);
    }

    /* ---------------- 当前用户 ---------------- */

    @GetMapping({"/auth/current", "/current"})
    public R<Map<String, Object>> current(){
        AdminContext.Ctx c = AdminContext.get();
        if (c == null) return R.fail(401, "未登录");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("user", c.user);
        m.put("name", c.name);
        m.put("role", c.roleKey);
        m.put("permCount", c.perms == null ? 0 : c.perms.size());
        m.put("roleName", roleName(c.roleKey));
        m.put("perms", new ArrayList<>(c.perms));
        m.put("ip", c.ip);
        return R.ok(m);
    }

    /** 我的权限码列表（前端菜单渲染用） */
    @GetMapping("/auth/perms")
    public R<List<String>> perms(){
        AdminContext.Ctx c = AdminContext.get();
        return R.ok(c == null ? new ArrayList<>() : new ArrayList<>(c.perms));
    }

    /* ---------------- 修改密码 ---------------- */

    public static class ChangePwdRequest {
        private String oldPass;
        private String newPass;
        public String getOldPass(){ return oldPass; }
        public void setOldPass(String v){ this.oldPass = v; }
        public String getNewPass(){ return newPass; }
        public void setNewPass(String v){ this.newPass = v; }
    }

    /**
     * 修改自己的密码
     * 改成功后会踢掉该账号的其它会话（安全惯例：防止别人拿着旧会话继续用）
     */
    @PostMapping({"/auth/password", "/password"})
    public R<Boolean> changePassword(@RequestBody ChangePwdRequest req){
        AdminContext.Ctx c = AdminContext.get();
        if (c == null || c.user == null) return R.fail(401,"未登录");
        if (req == null) return R.fail(400,"参数为空");

        AdminAccount acc = authMapper.selectByUser(c.user);
        if (acc == null) return R.fail(400,"账号不存在");

        /* ① 旧密码必须正确（会话被劫持时也改不了密码） */
        String old = req.getOldPass() == null ? "" : req.getOldPass();
        if (!PasswordKit.matches(old, acc.getPass())) return R.fail(400,"原密码不正确");

        /* ② 新密码强度 */
        String np = req.getNewPass() == null ? "" : req.getNewPass();
        String err = PasswordKit.checkStrength(np);
        if (err != null) return R.fail(400, err);
        if (PasswordKit.matches(np, acc.getPass())) return R.fail(400,"新密码不能与原密码相同");

        /* ③ 写 BCrypt */
        authMapper.updatePass(c.user, PasswordKit.encode(np));

        /* ④ 踢掉该账号其它会话，只保留当前这个 */
        try{ authMapper.deleteSessionByUser(c.user); }catch(Exception ignore){}
        try{ authMapper.insertSession(restoreCurrent(c)); }catch(Exception ignore){}

        return R.ok(true);
    }

    /* ★ 中文角色名 → role_key（兼容未跑迁移 SQL 的老数据） */
    private static String normalizeRole(String role){
        if (role == null || role.trim().isEmpty()) return "viewer";
        String r = role.trim();
        if ("super".equals(r) || "editor".equals(r) || "viewer".equals(r)) return r;
        if (r.contains("超级") || r.contains("管理")) return "super";
        if (r.contains("运营") || r.contains("编辑")) return "editor";
        return "viewer";
    }

    /** 把当前会话重新写回（上面整删了） */
    private AdminSession restoreCurrent(AdminContext.Ctx c){
        AdminSession s = new AdminSession();
        s.setToken(c.token);
        s.setUser(c.user);
        s.setName(c.name);
        s.setRoleKey(c.roleKey);
        s.setIp(c.ip);
        s.setExpireAt(LocalDateTime.now().plusHours(8));
        return s;
    }

    /* ---------------- 在线会话 ---------------- */

    @RequirePerm(value = "audit:view", module = "audit", desc = "查看在线会话")
    @GetMapping("/auth/sessions")
    public R<List<AdminSession>> sessions(){
        return R.ok(authMapper.selectOnlineSessions());
    }

    /** 强制下线 */
    @RequirePerm(value = "account:edit", module = "account", desc = "强制账号下线")
    @DeleteMapping("/auth/sessions/{token}")
    public R<Boolean> kick(@PathVariable("token") String token){
        return R.ok(authMapper.deleteSession(token) > 0);
    }

    /* ---------------- 辅助 ---------------- */

    private static final Map<String,String> RN = new HashMap<>();
    static {
        RN.put("super","超级管理员");
        RN.put("editor","运营编辑");
        RN.put("viewer","只读访客");
    }
    private String roleName(String key){
        String v = RN.get(key);
        return v == null ? (key == null ? "" : key) : v;
    }

    private String clientIp(HttpServletRequest req){
        String[] heads = {"X-Forwarded-For","X-Real-IP"};
        for (String h : heads){
            String v = req.getHeader(h);
            if (v != null && !v.isEmpty() && !"unknown".equalsIgnoreCase(v)) return v.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
