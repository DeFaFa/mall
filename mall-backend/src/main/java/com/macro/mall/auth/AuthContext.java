package com.macro.mall.auth;

import com.macro.mall.common.UnauthorizedException;

public final class AuthContext {
    private static final ThreadLocal<Long> ADMIN_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> MEMBER_ID = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void setAdminId(Long id) {
        ADMIN_ID.set(id);
    }

    public static Long getAdminId() {
        return ADMIN_ID.get();
    }

    public static void setMemberId(Long id) {
        MEMBER_ID.set(id);
    }

    public static Long getMemberId() {
        return MEMBER_ID.get();
    }

    public static Long requireAdminId() {
        Long id = ADMIN_ID.get();
        if (id == null) {
            throw new UnauthorizedException("管理员未登录");
        }
        return id;
    }

    public static Long requireMemberId() {
        Long id = MEMBER_ID.get();
        if (id == null) {
            throw new UnauthorizedException("会员未登录");
        }
        return id;
    }

    public static void clear() {
        ADMIN_ID.remove();
        MEMBER_ID.remove();
    }
}
