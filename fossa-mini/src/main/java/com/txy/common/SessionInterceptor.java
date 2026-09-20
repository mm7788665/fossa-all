package com.txy.common;

import com.txy.fossa.web.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * 登录拦截器：沿用小程序 sessionKey 机制
 *   - 前端 header 带 sessionId（管理后台登录后由后端写入）
 *   - 白名单路径（/api/login、/api/upload/**）不做校验
 */
@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Autowired
    private AdminService adminService;

    private static final Set<String> WHITE = new java.util.HashSet<String>() {{
        add("/api/login");
        add("/api/logout");
        add("/api/upload");      // /api/upload/image
        add("/uploads");         // 静态资源访问
        add("/error");
    }};

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (WHITE.stream().anyMatch(uri::startsWith)) {
            return true;
        }
        String sid = request.getHeader("sessionId");
        if (sid == null || sid.isEmpty()) {
            sid = request.getParameter("sessionId");   // 兼容 uploadFile(formData)
        }
        if (sid != null && adminService.checkSession(sid)) {
            request.setAttribute("adminUser", adminService.getBySession(sid));
            return true;
        }
        response.setStatus(401);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"statusCode\":401,\"message\":\"未登录或登录已过期\"}");
        return false;
    }
}
