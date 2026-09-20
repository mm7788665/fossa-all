package com.txy.fossa.web.service;

import com.txy.fossa.web.mapper.AdminMapper;
import com.txy.fossa.web.vo.DashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理后台服务：登录 Session + 控制台统计
 *
 * Session 机制（沿用小程序 sessionKey 思路）：
 *   - 登录成功生成 sessionId，存入内存 Map（生产建议换成 Redis）
 *   - 请求 header 带 sessionId 走拦截器校验
 */
@Service
public class AdminService {

    @Autowired
    private AdminMapper adminMapper;

    /** sessionId -> 账号。生产环境请换成 Redis */
    private static final Map<String, String> SESSIONS = new ConcurrentHashMap<>();

    public String login(String user, String pass) {
        // 密码校验（生产环境必须加盐哈希，此处为对齐现有 admin/123456 逻辑）
        String dbPass = adminMapper.selectPassByUser(user);
        if (dbPass == null) {
            return null;
        }
        // 支持明文（演示）或 BCrypt
        if (dbPass.equals(pass) || (dbPass.startsWith("$2") && matchesBcrypt(pass, dbPass))) {
            String sid = "sess_" + UUID.randomUUID().toString().replace("-", "");
            SESSIONS.put(sid, user);
            adminMapper.updateLastLogin(user);
            return sid;
        }
        return null;
    }

    public boolean checkSession(String sessionId) {
        return SESSIONS.containsKey(sessionId);
    }

    public String getBySession(String sessionId) {
        return SESSIONS.get(sessionId);
    }

    public void logout(String sessionId) {
        SESSIONS.remove(sessionId);
    }

    public DashboardVO dashboard() {
        DashboardVO vo = new DashboardVO();
        Map<String, Object> stats = adminMapper.selectDashboard();
        if (stats != null) {
            vo.setNpcCount(intVal(stats.get("npc_count")));
            vo.setShowCount(intVal(stats.get("show_count")));
            vo.setShowOnSale(intVal(stats.get("show_on_sale")));
            vo.setOrderCount(intVal(stats.get("order_count")));
            vo.setPaidCount(intVal(stats.get("paid_count")));
            vo.setPlayerCount(intVal(stats.get("player_count")));
            vo.setRevenue(longVal(stats.get("revenue")));
            vo.setSeatsTaken(intVal(stats.get("seats_taken")));
            vo.setSeatsTotal(intVal(stats.get("seats_total")));
            vo.setNoticePublished(intVal(stats.get("notice_published")));
        }
        // 亲密度：取「me」玩家总和（对齐前端 HT.stats().intimacy）
        Integer intimacy = adminMapper.selectIntimacyTotal("me");
        vo.setIntimacy(intimacy == null ? 0 : intimacy);

        vo.setPopularity(adminMapper.selectNpcPopularity());
        vo.setSeatRate(adminMapper.selectShowSeat());
        vo.setRecentOrders(adminMapper.selectRecentOrders(6));
        return vo;
    }

    // ---- 工具 ----
    private int intVal(Object o) {
        return o == null ? 0 : ((Number) o).intValue();
    }
    private long longVal(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }
    private boolean matchesBcrypt(String raw, String hashed) {
        try {
            return org.springframework.security.crypto.bcrypt.BCrypt.checkpw(raw, hashed);
        } catch (Exception e) {
            return false;
        }
    }
}
