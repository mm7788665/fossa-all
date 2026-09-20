package com.txy.service;

import com.txy.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.txy.entity.AdminAccount;
import com.txy.mapper.AdminAccountMapper;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private AdminAccountMapper adminAccountMapper;

    /**
     * 登录：已逻辑删除的账号不能登录（SQL 已带 deleted = 0）
     */
    public Map<String, Object> login(LoginRequest req) {
        AdminAccount acc = adminAccountMapper.selectByUserAndPass(req.getUser(), req.getPass());
        if (acc == null) {
            throw new RuntimeException("账号或密码错误（演示：admin / 123456）");
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("user", acc.getUser());
        data.put("role", acc.getRole());
        data.put("name", acc.getName());
        return data;
    }
}
