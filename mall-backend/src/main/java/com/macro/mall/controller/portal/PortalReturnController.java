package com.macro.mall.controller.portal;

import com.macro.mall.auth.AuthContext;
import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.OrderReturnApply;
import com.macro.mall.entity.OrderReturnReason;
import com.macro.mall.service.PortalOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portal/return")
@RequiredArgsConstructor
public class PortalReturnController {
    private final PortalOrderService portalOrderService;

    @GetMapping("/reasons")
    public CommonResult<List<OrderReturnReason>> reasons() {
        return CommonResult.success(portalOrderService.returnReasons());
    }

    @GetMapping("/list")
    public CommonResult<List<OrderReturnApply>> list() {
        return CommonResult.success(portalOrderService.returns(AuthContext.requireMemberId()));
    }

    @PostMapping("/apply")
    public CommonResult<Long> apply(@RequestBody OrderReturnApply apply) {
        return CommonResult.success(portalOrderService.applyReturn(AuthContext.requireMemberId(), apply));
    }
}
