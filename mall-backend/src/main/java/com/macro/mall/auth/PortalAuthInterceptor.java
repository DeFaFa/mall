package com.macro.mall.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.macro.mall.common.CommonResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PortalAuthInterceptor implements HandlerInterceptor {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final TokenService tokenService;

    public PortalAuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = TokenUtils.resolve(request);
        Long memberId = tokenService.getMemberId(token);
        if (memberId == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(MAPPER.writeValueAsString(CommonResult.failed(401, "请先登录会员账号")));
            return false;
        }
        AuthContext.setMemberId(memberId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
