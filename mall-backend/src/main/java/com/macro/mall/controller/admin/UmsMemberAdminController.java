package com.macro.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macro.mall.common.CommonPage;
import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.Member;
import com.macro.mall.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/member")
@RequiredArgsConstructor
public class UmsMemberAdminController {
    private final MemberMapper memberMapper;

    @GetMapping("/list")
    public CommonResult<CommonPage<Member>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return CommonResult.success(CommonPage.of(memberMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Member>()
                        .and(keyword != null && !keyword.isBlank(), w -> w.like(Member::getUsername, keyword)
                                .or().like(Member::getNickname, keyword)
                                .or().like(Member::getPhone, keyword))
                        .orderByDesc(Member::getId))));
    }

    @GetMapping("/{id}")
    public CommonResult<Member> detail(@PathVariable Long id) {
        return CommonResult.success(memberMapper.selectById(id));
    }

    @PostMapping("/updateStatus")
    public CommonResult<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        Member member = new Member();
        member.setId(id);
        member.setStatus(status);
        memberMapper.updateById(member);
        return CommonResult.success();
    }
}
