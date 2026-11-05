package com.macro.mall.controller.admin;

import com.macro.mall.common.CommonResult;
import com.macro.mall.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final AdminOrderService adminOrderService;

    @GetMapping
    public CommonResult<Map<String, Object>> dashboard() {
        return CommonResult.success(adminOrderService.dashboard());
    }
}
