package com.macro.mall.controller.portal;

import com.macro.mall.auth.AuthContext;
import com.macro.mall.common.CommonResult;
import com.macro.mall.dto.CartAddParam;
import com.macro.mall.dto.CartItemView;
import com.macro.mall.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portal/cart")
@RequiredArgsConstructor
public class PortalCartController {
    private final CartService cartService;

    @GetMapping("/list")
    public CommonResult<List<CartItemView>> list() {
        return CommonResult.success(cartService.list(AuthContext.requireMemberId()));
    }

    @PostMapping("/add")
    public CommonResult<Void> add(@RequestBody CartAddParam param) {
        cartService.add(AuthContext.requireMemberId(), param);
        return CommonResult.success();
    }

    @PostMapping("/updateQuantity")
    public CommonResult<Void> updateQuantity(@RequestParam Long id, @RequestParam Integer quantity) {
        cartService.updateQuantity(AuthContext.requireMemberId(), id, quantity);
        return CommonResult.success();
    }

    @PostMapping("/updateSku")
    public CommonResult<Void> updateSku(@RequestParam Long id, @RequestParam Long skuId) {
        cartService.updateSku(AuthContext.requireMemberId(), id, skuId);
        return CommonResult.success();
    }

    @PostMapping("/delete")
    public CommonResult<Void> delete(@RequestBody List<Long> ids) {
        cartService.delete(AuthContext.requireMemberId(), ids);
        return CommonResult.success();
    }

    @PostMapping("/clear")
    public CommonResult<Void> clear() {
        cartService.clear(AuthContext.requireMemberId());
        return CommonResult.success();
    }
}
