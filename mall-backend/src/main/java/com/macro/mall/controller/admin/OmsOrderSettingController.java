package com.macro.mall.controller.admin;

import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.OrderSetting;
import com.macro.mall.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orderSetting")
@RequiredArgsConstructor
public class OmsOrderSettingController {
    private final AdminOrderService adminOrderService;

    @GetMapping
    public CommonResult<OrderSetting> get() {
        return CommonResult.success(adminOrderService.orderSetting());
    }

    @PostMapping("/update")
    public CommonResult<Void> update(@RequestBody OrderSetting setting) {
        adminOrderService.updateOrderSetting(setting);
        return CommonResult.success();
    }
}
