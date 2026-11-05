package com.macro.mall.controller.portal;

import com.macro.mall.auth.AuthContext;
import com.macro.mall.common.CommonResult;
import com.macro.mall.dto.CartItemView;
import com.macro.mall.dto.CouponHistoryView;
import com.macro.mall.entity.Coupon;
import com.macro.mall.service.CartService;
import com.macro.mall.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portal/coupon")
@RequiredArgsConstructor
public class PortalCouponController {
    private final CouponService couponService;
    private final CartService cartService;

    @GetMapping("/list")
    public CommonResult<List<Coupon>> list() {
        return CommonResult.success(couponService.availableList());
    }

    @GetMapping("/my")
    public CommonResult<List<CouponHistoryView>> my(@RequestParam(required = false) Integer useStatus) {
        return CommonResult.success(couponService.myCoupons(AuthContext.requireMemberId(), useStatus));
    }

    @PostMapping("/receive/{id}")
    public CommonResult<Void> receive(@PathVariable Long id) {
        couponService.receive(AuthContext.requireMemberId(), id);
        return CommonResult.success();
    }

    @PostMapping("/available")
    public CommonResult<List<CouponHistoryView>> available(@RequestBody List<Long> cartIds) {
        Long memberId = AuthContext.requireMemberId();
        List<CartItemView> items = cartService.list(memberId).stream()
                .filter(i -> cartIds == null || cartIds.isEmpty() || cartIds.contains(i.getId()))
                .toList();
        return CommonResult.success(couponService.usableForCart(memberId, items));
    }
}
