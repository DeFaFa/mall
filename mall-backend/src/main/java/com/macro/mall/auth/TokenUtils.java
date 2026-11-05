package com.macro.mall.auth;

import jakarta.servlet.http.HttpServletRequest;

public final class TokenUtils {
    private TokenUtils() {
    }

    public static String resolve(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && !header.isBlank()) {
            return header.startsWith("Bearer ") ? header.substring(7) : header;
        }
        String token = request.getHeader("token");
        if (token != null && !token.isBlank()) {
            return token;
        }
        return request.getParameter("token");
    }
}
