package com.macro.mall.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 公开商品接口的可选登录态：带会员 token 时识别会员（会员价、浏览足迹），
 * 不带 token 也放行。
 */
@Component
public class OptionalMemberInterceptor implements HandlerInterceptor {
    private final TokenService tokenService;

    public OptionalMemberInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = TokenUtils.resolve(request);
        Long memberId = tokenService.getMemberId(token);
        if (memberId != null) {
            AuthContext.setMemberId(memberId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
