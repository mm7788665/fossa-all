package com.txy.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.txy.dto.R;
import com.txy.entity.AdminAuditLog;
import com.txy.entity.AdminSession;
import com.txy.mapper.AdminAuditMapper;
import com.txy.mapper.AdminAuthMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 后台统一认证 + 鉴权 + 审计拦截器
 *
 * 职责（按执行顺序）：
 *  ① preHandle
 *     - @PublicApi 直接放行（登录接口等）
 *     - 取 token（优先 Header X-Token，其次参数/ Cookie）
 *     - 查 admin_session：不存在或已过期 → 401
 *     - 滑动续期：每次请求把过期时间往后推（默认 8 小时）
 *     - 装载 AdminContext（账号/姓名/角色/权限集合）
 *     - @RequirePerm 校验：无权限 → 403
 *  ② afterCompletion
 *     - 写操作（POST/PUT/DELETE）落审计日志，异步入库，不影响响应速度
 *     - 清理 ThreadLocal（避免线程复用导致串号）
 *
 * 返回体统一走 R，前端 api.js 已能识别 {code,msg,data}。
 * 特别地：401 用 code=401，前端会跳登录页；403 用 code=403，弹提示。
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    /** token 请求头名 */
    public static final String TOKEN_HEADER = "X-Token";
    /** 会话有效期（小时），滑动续期 */
    public static final int SESSION_HOURS = 8;

    private final AdminAuthMapper authMapper;
    private final AdminAuditMapper auditMapper;

    public AdminAuthInterceptor(AdminAuthMapper authMapper, AdminAuditMapper auditMapper){
        this.authMapper = authMapper;
        this.auditMapper = auditMapper;
    }

    /** 审计日志异步写库，避免拖慢接口 */
    private final ExecutorService auditPool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "audit-logger");
        t.setDaemon(true);
        return t;
    });

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {

        /* 非 Controller 方法（静态资源等）直接放行 */
        if (!(handler instanceof HandlerMethod)) return true;
        HandlerMethod hm = (HandlerMethod) handler;

        /* ① 免登录接口 */
        if (hm.hasMethodAnnotation(PublicApi.class)
                || hm.getBeanType().isAnnotationPresent(PublicApi.class)){
            return true;
        }

        /* ② 取 token */
        String token = req.getHeader(TOKEN_HEADER);
        if (token == null || token.trim().isEmpty()) token = req.getParameter("token");
        if (token == null || token.trim().isEmpty()){
            javax.servlet.http.Cookie[] cs = req.getCookies();
            if (cs != null) for (javax.servlet.http.Cookie c : cs){
                if ("ht_token".equals(c.getName())){ token = c.getValue(); break; }
            }
        }
        if (token == null || token.trim().isEmpty()){
            write(resp, 401, "未登录或登录已失效，请重新登录");
            return false;
        }

        /* ③ 校验会话 */
        AdminSession s = authMapper.selectSession(token.trim());
        if (s == null || s.expired()){
            if (s != null) authMapper.deleteSession(s.getToken());
            write(resp, 401, "登录已失效，请重新登录");
            return false;
        }

        /* ④ 滑动续期 */
        authMapper.touchSession(s.getToken(), LocalDateTime.now().plusHours(SESSION_HOURS));

        /* ⑤ 装载上下文（权限集合一次查好，方法鉴权直接用内存比对） */
        AdminContext.Ctx ctx = new AdminContext.Ctx();
        ctx.user    = s.getUser();
        ctx.name    = s.getName();
        ctx.roleKey = s.getRoleKey();
        ctx.token   = s.getToken();
        ctx.ip      = clientIp(req);
        try{
            List<String> perms = authMapper.selectPermsByRole(s.getRoleKey());
            ctx.perms = (perms == null) ? Collections.emptySet() : new HashSet<>(perms);
        }catch(Exception ignore){
            ctx.perms = Collections.emptySet();
        }
        /* super 内置通配，防止权限表被清空后超级管理员也进不去 */
        if ("super".equals(s.getRoleKey())) ctx.perms.add("*");
        AdminContext.set(ctx);

        /* ⑥ 方法级鉴权 */
        RequirePerm rp = hm.getMethodAnnotation(RequirePerm.class);
        if (rp == null) rp = hm.getBeanType().getAnnotation(RequirePerm.class);
        if (rp != null && !AdminContext.has(rp.value())){
            write(resp, 403, "没有操作权限（需要 " + rp.value() + "）");
            return false;
        }

        /* 记录审计起点（耗时） */
        req.setAttribute("_audit_start", System.currentTimeMillis());
        req.setAttribute("_audit_rp", rp);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse resp, Object handler, Exception ex){
        try{
            if (!(handler instanceof HandlerMethod)) return;
            HandlerMethod hm = (HandlerMethod) handler;
            if (hm.hasMethodAnnotation(PublicApi.class)) return;

            String method = req.getMethod();
            boolean writeOp = "POST".equals(method) || "PUT".equals(method)
                           || "DELETE".equals(method) || "PATCH".equals(method);
            if (!writeOp) return;                       // ★ 关键：只记写操作

            AdminContext.Ctx c = AdminContext.get();
            if (c == null) return;

            Long st = (Long) req.getAttribute("_audit_start");
            int cost = st == null ? 0 : (int)(System.currentTimeMillis() - st);

            RequirePerm rp = (RequirePerm) req.getAttribute("_audit_rp");
            String uri = req.getRequestURI();
            String[] pair = guessModule(uri, rp);
            String action = guessAction(method, uri, rp);
            String targetId = guessTargetId(uri);
            String summary = buildSummary(c, pair[1], action, targetId, rp);

            AdminAuditLog log = AdminAuditLog.of(
                    c.user, c.name, pair[0], pair[1], action, targetId, summary,
                    safeParams(req), method, uri, c.ip);
            log.setCostMs(cost);
            /* 响应码 ≥400 视为失败 */
            if (resp.getStatus() >= 400){ log.setSuccess(0); log.setMsg("HTTP " + resp.getStatus()); }
            if (ex != null){ log.setSuccess(0); log.setMsg(ex.getMessage()); }

            auditPool.submit(() -> {
                try{ auditMapper.insert(log); }catch(Exception ignore){}
            });
        }finally{
            /* ★ 必须清理，否则线程池复用会串号 */
            AdminContext.clear();
        }
    }

    /* ---------------- 辅助 ---------------- */

    /** 模块推断：优先用注解，其次从 URI 第二段猜（/api/gacha/pools → gacha） */
    private String[] guessModule(String uri, RequirePerm rp){
        if (rp != null && !rp.module().isEmpty()){
            String m = rp.module();
            return new String[]{ m, moduleName(m) };
        }
        String m = "";
        String[] seg = uri.split("/");
        for (String s : seg){
            if ("api".equals(s) || s.isEmpty()) continue;
            m = s; break;
        }
        if (m.isEmpty() && rp != null && !rp.value().isEmpty()){
            int i = rp.value().indexOf(':');
            if (i > 0) m = rp.value().substring(0, i);
        }
        return new String[]{ m, moduleName(m) };
    }

    private static final Map<String,String> MODULE_NAME = new HashMap<>();
    static {
        MODULE_NAME.put("npc","角色管理");
        MODULE_NAME.put("show","场次管理");
        MODULE_NAME.put("order","订单管理");
        MODULE_NAME.put("notice","公告管理");
        MODULE_NAME.put("player","玩家管理");
        MODULE_NAME.put("intimacy","亲密度");
        MODULE_NAME.put("gacha","抽卡奖池");
        MODULE_NAME.put("setting","系统设置");
        MODULE_NAME.put("dashboard","控制台");
        MODULE_NAME.put("account","账号管理");
        MODULE_NAME.put("role","角色权限");
        MODULE_NAME.put("audit","操作日志");
        MODULE_NAME.put("auth","登录认证");
        MODULE_NAME.put("upload","图片上传");
    }
    private String moduleName(String m){
        String v = MODULE_NAME.get(m);
        return v == null ? (m == null ? "" : m) : v;
    }

    private String guessAction(String method, String uri, RequirePerm rp){
        if (rp != null && !rp.desc().isEmpty()) return rp.desc();
        if ("POST".equals(method))   return uri.endsWith("/login") ? "login" : "add";
        if ("PUT".equals(method))    return "update";
        if ("DELETE".equals(method)) return "delete";
        if ("PATCH".equals(method))  return "update";
        return method.toLowerCase();
    }

    /** 从 URI 里取对象ID：/api/npcs/n1 → n1 */
    private String guessTargetId(String uri){
        String[] seg = uri.split("/");
        if (seg.length == 0) return "";
        String last = seg[seg.length - 1];
        if (last == null || last.isEmpty()) return "";
        /* 排除纯动作路径，如 /status、/weight */
        if ("login".equals(last) || "logout".equals(last) || "status".equals(last)
            || "weight".equals(last) || "list".equals(last)) return "";
        /* 看起来像 ID：含数字或下划线，或长度≥2 且非纯中文动作 */
        return last.length() <= 64 ? last : last.substring(0, 64);
    }

    private String buildSummary(AdminContext.Ctx c, String moduleName, String action,
                                String targetId, RequirePerm rp){
        String who = (c.name != null && !c.name.isEmpty()) ? c.name : c.user;
        String what = (rp != null && !rp.desc().isEmpty()) ? rp.desc() : action;
        return who + " " + what + (moduleName.isEmpty() ? "" : "（" + moduleName + "）")
                + (targetId == null || targetId.isEmpty() ? "" : "：" + targetId);
    }

    /** 参数脱敏：pass/password/token 一律打码 */
    private String safeParams(HttpServletRequest req){
        try{
            Map<String, String[]> m = req.getParameterMap();
            Map<String, Object> out = new LinkedHashMap<>();
            if (m != null) for (Map.Entry<String, String[]> e : m.entrySet()){
                String k = e.getKey();
                String v = (e.getValue() != null && e.getValue().length > 0) ? e.getValue()[0] : "";
                out.put(k, mask(k, v));
            }
            return out.isEmpty() ? "" : om.writeValueAsString(out);
        }catch(Exception e){
            return "";
        }
    }
    private String mask(String k, String v){
        String lk = k.toLowerCase();
        if (lk.contains("pass") || lk.contains("token") || lk.contains("secret")) return "******";
        if (v != null && v.length() > 500) return v.substring(0, 500) + "…(截断)";
        return v;
    }

    private String clientIp(HttpServletRequest req){
        String[] heads = {"X-Forwarded-For","X-Real-IP","Proxy-Client-IP","WL-Proxy-Client-IP"};
        for (String h : heads){
            String v = req.getHeader(h);
            if (v != null && !v.isEmpty() && !"unknown".equalsIgnoreCase(v)){
                return v.split(",")[0].trim();
            }
        }
        return req.getRemoteAddr();
    }

    private void write(HttpServletResponse resp, int code, String msg) throws Exception {
        resp.setStatus(code);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(om.writeValueAsString(R.fail(code, msg)));
    }
}
