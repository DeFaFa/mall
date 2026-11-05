package com.macro.mall.controller.portal;

import com.macro.mall.auth.AuthContext;
import com.macro.mall.common.CommonResult;
import com.macro.mall.dto.OrderPayResult;
import com.macro.mall.service.PortalOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 支付宝支付演示。
 * 生产接入时把 pay 换成 AlipayClient.pageExecute，并在 notify 中做 RSA 验签。
 */
@RestController
@RequestMapping("/portal/alipay")
@RequiredArgsConstructor
public class PortalAlipayController {
    private final PortalOrderService portalOrderService;

    @GetMapping("/pay/{orderId}")
    public CommonResult<OrderPayResult> pay(@PathVariable Long orderId) {
        return CommonResult.success(portalOrderService.pay(AuthContext.requireMemberId(), orderId));
    }

    @RequestMapping(value = "/notify", method = {RequestMethod.GET, RequestMethod.POST})
    public String notify(@RequestParam Long orderId,
                         @RequestParam(required = false) String tradeNo) {
        portalOrderService.paySuccess(orderId, tradeNo);
        return "success";
    }
}
