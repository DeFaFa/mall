package com.macro.mall.controller.portal;

import com.macro.mall.auth.AuthContext;
import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.MemberProductCollection;
import com.macro.mall.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portal/collection")
@RequiredArgsConstructor
public class PortalCollectionController {
    private final MemberService memberService;

    @GetMapping("/list")
    public CommonResult<List<MemberProductCollection>> list() {
        return CommonResult.success(memberService.collections(AuthContext.requireMemberId()));
    }

    @PostMapping("/add/{productId}")
    public CommonResult<Void> add(@PathVariable Long productId) {
        memberService.addCollection(AuthContext.requireMemberId(), productId);
        return CommonResult.success();
    }

    @PostMapping("/delete/{productId}")
    public CommonResult<Void> delete(@PathVariable Long productId) {
        memberService.deleteCollection(AuthContext.requireMemberId(), productId);
        return CommonResult.success();
    }
}
