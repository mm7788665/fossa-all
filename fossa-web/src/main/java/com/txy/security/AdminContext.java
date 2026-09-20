package com.txy.security;

import java.util.Collections;
import java.util.Set;

/**
 * 当前登录管理员上下文（ThreadLocal）
 *
 * 在拦截器里 set，请求结束后 clear。
 * Controller / Service 里需要"谁在操作"时直接 AdminContext.getUser()。
 */
public final class AdminContext {

    private static final ThreadLocal<Ctx> TL = new ThreadLocal<>();

    public static final class Ctx {
        public String user;
        public String name;
        public String roleKey;
        public String token;
        public String ip;
        public Set<String> perms = Collections.emptySet();
    }

    public static void set(Ctx c){ TL.set(c); }

    public static Ctx get(){ return TL.get(); }

    public static void clear(){ TL.remove(); }

    /** 当前账号，未登录返回 null */
    public static String getUser(){
        Ctx c = TL.get();
        return c == null ? null : c.user;
    }

    public static String getName(){
        Ctx c = TL.get();
        return c == null ? "" : (c.name == null ? "" : c.name);
    }

    public static String getRoleKey(){
        Ctx c = TL.get();
        return c == null ? null : c.roleKey;
    }

    public static String getToken(){
        Ctx c = TL.get();
        return c == null ? null : c.token;
    }

    public static String getIp(){
        Ctx c = TL.get();
        return c == null ? "" : (c.ip == null ? "" : c.ip);
    }

    /** 是否拥有某权限 */
    public static boolean has(String permCode){
        if (permCode == null || permCode.isEmpty()) return true;
        Ctx c = TL.get();
        if (c == null) return false;
        if (c.perms == null) return false;
        /* 通配：拥有 * 表示超级管理员 */
        if (c.perms.contains("*")) return true;
        if (c.perms.contains(permCode)) return true;
        /* 模块级通配：拥有 npc:* 等价于 npc 下所有动作 */
        int i = permCode.indexOf(':');
        return i > 0 && c.perms.contains(permCode.substring(0, i) + ":*");
    }
}
