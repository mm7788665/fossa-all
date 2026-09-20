package com.txy.mapper;

import com.txy.entity.AdminAccount;
import com.txy.entity.AdminSession;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 账号与登录会话 */
public interface AdminAuthMapper {

    /** 按账号查（不含已删除） */
    AdminAccount selectByUser(@Param("user") String user);

    /** 修改密码（存 BCrypt 密文） */
    int updatePass(@Param("user") String user, @Param("pass") String pass);

    /** 更新最后登录时间 */
    int updateLastLogin(@Param("user") String user);

    /** 会话：新增 */
    int insertSession(AdminSession s);

    /** 会话：按 token 查 */
    AdminSession selectSession(@Param("token") String token);

    /** 会话：刷新活跃时间（顺便续期，滑动过期） */
    int touchSession(@Param("token") String token, @Param("expireAt") java.time.LocalDateTime expireAt);

    /** 会话：删除（登出） */
    int deleteSession(@Param("token") String token);

    /** 会话：删该账号的所有会话（改密码/禁用时踢下线） */
    int deleteSessionByUser(@Param("user") String user);

    /** 清理过期会话 */
    int deleteExpiredSessions();

    /** 在线会话列表 */
    List<AdminSession> selectOnlineSessions();

    /** 该账号拥有的权限码 */
    List<String> selectPermsByRole(@Param("roleKey") String roleKey);
}
