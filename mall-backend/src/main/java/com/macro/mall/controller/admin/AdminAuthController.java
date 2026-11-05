package com.macro.mall.controller.admin;

import com.macro.mall.auth.TokenUtils;
import com.macro.mall.common.CommonResult;
import com.macro.mall.dto.AdminInfo;
import com.macro.mall.dto.LoginParam;
import com.macro.mall.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAuthController {
    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public CommonResult<Map<String, Object>> login(@RequestBody LoginParam param) {
        return CommonResult.success(adminAuthService.login(param.getUsername(), param.getPassword()));
    }

    @GetMapping("/info")
    public CommonResult<AdminInfo> info() {
        return CommonResult.success(adminAuthService.getInfo(com.macro.mall.auth.AuthContext.requireAdminId()));
    }

    @PostMapping("/logout")
    public CommonResult<Void> logout(HttpServletRequest request) {
        adminAuthService.logout(TokenUtils.resolve(request));
        return CommonResult.success();
    }
}
