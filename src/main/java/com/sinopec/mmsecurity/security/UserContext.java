package com.sinopec.mmsecurity.security;

/**
 * 当前线程登录态持有者（ThreadLocal）。
 * JwtFilter 写入，Service 读取，finally 清理。
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {}

    public static void set(LoginUser u) { HOLDER.set(u); }

    public static LoginUser get() { return HOLDER.get(); }

    public static void clear() { HOLDER.remove(); }

    public static Long userId() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getUserId();
    }

    public static String username() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getUsername();
    }

    public static String role() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getRole();
    }

    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role());
    }
}
