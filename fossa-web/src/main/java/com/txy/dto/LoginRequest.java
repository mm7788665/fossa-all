package com.txy.dto;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求
 * 注意：Spring Boot 2.7 用 javax.validation；Boot 3.x 才用 jakarta.validation
 */
public class LoginRequest {

    @NotBlank(message = "账号不能为空")
    private String user;

    @NotBlank(message = "密码不能为空")
    private String pass;

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }
}
