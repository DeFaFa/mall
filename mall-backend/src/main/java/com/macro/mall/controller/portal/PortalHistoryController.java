package com.macro.mall.controller.portal;

import com.macro.mall.auth.AuthContext;
import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.MemberBrowseHistory;
import com.macro.mall.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portal/history")
@RequiredArgsConstructor
public class PortalHistoryController {
    private final MemberService memberService;

    @GetMapping("/list")
    public CommonResult<List<MemberBrowseHistory>> list() {
        return CommonResult.success(memberService.histories(AuthContext.requireMemberId()));
    }

    @PostMapping("/record/{productId}")
    public CommonResult<Void> record(@PathVariable Long productId) {
        memberService.recordHistory(AuthContext.requireMemberId(), productId);
        return CommonResult.success();
    }

    @PostMapping("/clear")
    public CommonResult<Void> clear() {
        memberService.clearHistories(AuthContext.requireMemberId());
        return CommonResult.success();
    }
}
