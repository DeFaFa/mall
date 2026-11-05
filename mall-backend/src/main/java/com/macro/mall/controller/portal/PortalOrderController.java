package com.macro.mall.controller.portal;

import com.macro.mall.auth.AuthContext;
import com.macro.mall.common.CommonPage;
import com.macro.mall.common.CommonResult;
import com.macro.mall.dto.OrderConfirmResult;
import com.macro.mall.dto.OrderDetail;
import com.macro.mall.dto.OrderParam;
import com.macro.mall.dto.OrderStatusCount;
import com.macro.mall.entity.Order;
import com.macro.mall.service.PortalOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portal/order")
@RequiredArgsConstructor
public class PortalOrderController {
    private final PortalOrderService portalOrderService;

    @PostMapping("/confirm")
    public CommonResult<OrderConfirmResult> confirm(@RequestBody OrderParam param) {
        return CommonResult.success(portalOrderService.confirm(AuthContext.requireMemberId(), param));
    }

    @PostMapping("/generate")
    public CommonResult<Long> generate(@RequestBody OrderParam param) {
        return CommonResult.success(portalOrderService.generate(AuthContext.requireMemberId(), param));
    }

    @GetMapping("/list")
    public CommonResult<CommonPage<Order>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        return CommonResult.success(CommonPage.of(portalOrderService.page(
                AuthContext.requireMemberId(), status, pageNum, pageSize)));
    }

    @GetMapping("/{id}")
    public CommonResult<OrderDetail> detail(@PathVariable Long id) {
        return CommonResult.success(portalOrderService.detail(AuthContext.requireMemberId(), id));
    }

    @PostMapping("/cancel/{id}")
    public CommonResult<Boolean> cancel(@PathVariable Long id) {
        return CommonResult.success(portalOrderService.cancel(AuthContext.requireMemberId(), id));
    }

    @PostMapping("/confirmReceive/{id}")
    public CommonResult<Void> confirmReceive(@PathVariable Long id) {
        portalOrderService.confirmReceive(AuthContext.requireMemberId(), id);
        return CommonResult.success();
    }

    @PostMapping("/delete/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        portalOrderService.delete(AuthContext.requireMemberId(), id);
        return CommonResult.success();
    }

    @GetMapping("/statusCount")
    public CommonResult<OrderStatusCount> statusCount() {
        return CommonResult.success(portalOrderService.statusCount(AuthContext.requireMemberId()));
    }
}
