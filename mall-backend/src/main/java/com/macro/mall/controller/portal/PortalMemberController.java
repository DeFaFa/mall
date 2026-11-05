package com.macro.mall.controller.portal;

import com.macro.mall.auth.AuthContext;
import com.macro.mall.common.CommonResult;
import com.macro.mall.dto.MemberInfo;
import com.macro.mall.entity.Member;
import com.macro.mall.service.FileStorageService;
import com.macro.mall.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/portal/member")
@RequiredArgsConstructor
public class PortalMemberController {
    private final MemberService memberService;
    private final FileStorageService fileStorageService;

    @GetMapping("/info")
    public CommonResult<MemberInfo> info() {
        return CommonResult.success(memberService.info(AuthContext.requireMemberId()));
    }

    @PostMapping("/update")
    public CommonResult<Void> update(@RequestBody Member member) {
        memberService.updateProfile(AuthContext.requireMemberId(), member);
        return CommonResult.success();
    }

    @PostMapping("/uploadAvatar")
    public CommonResult<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return CommonResult.success(fileStorageService.store(file));
    }
}
