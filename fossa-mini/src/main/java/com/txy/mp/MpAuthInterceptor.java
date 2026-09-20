package com.txy.mp;

import com.txy.fossa.web.entity.Player;
import com.txy.fossa.web.mp.mapper.MpMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 小程序登录态识别（软校验，不拦截）
 *  - 取 header / parameter 中的 sessionId
 *  - 查 player 表；命中则写入 request 属性 mpPlayerId
 *  - 未命中也不拦截：游客可浏览 NPC / 公告 / 场次 / 排行
 *    需要登录的接口由 MpBaseController.requirePlayerId() 自行抛 401
 */
@Component
public class MpAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private MpMapper mpMapper;

    public static final String KEY = "mpPlayerId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String sid = request.getHeader("sessionId");
        if (sid == null || sid.isEmpty()) {
            sid = request.getParameter("sessionId");
        }
        if (sid != null && !sid.isEmpty()) {
            try {
                Player p = mpMapper.selectPlayerById(sid);
                if (p != null) {
                    request.setAttribute(KEY, p.getId());
                }
            } catch (Exception ignored) {
                // 查询异常按游客处理，不影响内容接口
            }
        }
        return true;
    }
}
