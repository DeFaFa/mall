package com.macro.mall.controller.portal;

import com.macro.mall.auth.AuthContext;
import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.MemberBrandAttention;
import com.macro.mall.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portal/attention")
@RequiredArgsConstructor
public class PortalAttentionController {
    private final MemberService memberService;

    @GetMapping("/list")
    public CommonResult<List<MemberBrandAttention>> list() {
        return CommonResult.success(memberService.attentions(AuthContext.requireMemberId()));
    }

    @PostMapping("/add/{brandId}")
    public CommonResult<Void> add(@PathVariable Long brandId) {
        memberService.addAttention(AuthContext.requireMemberId(), brandId);
        return CommonResult.success();
    }

    @PostMapping("/delete/{brandId}")
    public CommonResult<Void> delete(@PathVariable Long brandId) {
        memberService.deleteAttention(AuthContext.requireMemberId(), brandId);
        return CommonResult.success();
    }
}
