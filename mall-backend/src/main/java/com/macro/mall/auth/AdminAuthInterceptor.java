package com.macro.mall.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.Resource;
import com.macro.mall.mapper.LinkMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private final TokenService tokenService;
    private final LinkMapper linkMapper;

    public AdminAuthInterceptor(TokenService tokenService, LinkMapper linkMapper) {
        this.tokenService = tokenService;
        this.linkMapper = linkMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 CORS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = TokenUtils.resolve(request);
        Long adminId = tokenService.getAdminId(token);
        if (adminId == null) {
            return reject(response, 401, "管理员未登录或登录已失效");
        }
        AuthContext.setAdminId(adminId);

        // 角色 id=1 视为超级管理员，拥有全部资源
        List<Long> roleIds = linkMapper.selectRoleIdsByAdmin(adminId);
        if (roleIds.contains(1L)) {
            return true;
        }
        List<Resource> resources = linkMapper.selectResourcesByAdmin(adminId);
        Set<String> allowed = resources.stream()
                .map(r -> r.getMethod() + " " + r.getUrl())
                .collect(Collectors.toSet());
        String method = request.getMethod();
        String uri = request.getRequestURI();
        boolean matchedResource = false;
        for (String rule : allowed) {
            int split = rule.indexOf(' ');
            String ruleMethod = rule.substring(0, split);
            String ruleUrl = rule.substring(split + 1);
            if (MATCHER.match(ruleUrl, uri) && ("*".equals(ruleMethod) || ruleMethod.equalsIgnoreCase(method))) {
                matchedResource = true;
                break;
            }
        }
        // 全局资源表中存在该接口但当前角色未授权，则拒绝
        List<Resource> allResources = linkMapper.selectAllResources();
        for (Resource resource : allResources) {
            if (MATCHER.match(resource.getUrl(), uri)
                    && ("*".equals(resource.getMethod()) || resource.getMethod().equalsIgnoreCase(method))) {
                if (!matchedResource) {
                    return reject(response, 403, "没有访问该接口的权限");
                }
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private boolean reject(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(MAPPER.writeValueAsString(CommonResult.failed(code, message)));
        return false;
    }
}
