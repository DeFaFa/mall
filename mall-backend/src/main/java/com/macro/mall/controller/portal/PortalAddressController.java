package com.macro.mall.controller.portal;

import com.macro.mall.auth.AuthContext;
import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.MemberReceiveAddress;
import com.macro.mall.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portal/address")
@RequiredArgsConstructor
public class PortalAddressController {
    private final MemberService memberService;

    @GetMapping("/list")
    public CommonResult<List<MemberReceiveAddress>> list() {
        return CommonResult.success(memberService.addresses(AuthContext.requireMemberId()));
    }

    @PostMapping("/create")
    public CommonResult<Void> create(@RequestBody MemberReceiveAddress address) {
        memberService.addAddress(AuthContext.requireMemberId(), address);
        return CommonResult.success();
    }

    @PostMapping("/update")
    public CommonResult<Void> update(@RequestBody MemberReceiveAddress address) {
        memberService.updateAddress(AuthContext.requireMemberId(), address);
        return CommonResult.success();
    }

    @PostMapping("/delete/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        memberService.deleteAddress(AuthContext.requireMemberId(), id);
        return CommonResult.success();
    }
}
