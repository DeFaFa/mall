package com.macro.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.MemberLevel;
import com.macro.mall.mapper.MemberLevelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/memberLevel")
@RequiredArgsConstructor
public class UmsMemberLevelController {
    private final MemberLevelMapper memberLevelMapper;

    @GetMapping("/list")
    public CommonResult<List<MemberLevel>> list() {
        return CommonResult.success(memberLevelMapper.selectList(new LambdaQueryWrapper<MemberLevel>()
                .orderByAsc(MemberLevel::getGrowthPoint)));
    }

    @PostMapping("/create")
    public CommonResult<Void> create(@RequestBody MemberLevel level) {
        if (level.getName() == null || level.getName().isBlank()) {
            return CommonResult.failed("等级名称不能为空");
        }
        if (level.getProductDiscount() == null) {
            level.setProductDiscount(new java.math.BigDecimal("10.00"));
        }
        memberLevelMapper.insert(level);
        return CommonResult.success();
    }

    @PostMapping("/update")
    public CommonResult<Void> update(@RequestBody MemberLevel level) {
        memberLevelMapper.updateById(level);
        return CommonResult.success();
    }

    @PostMapping("/delete/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        memberLevelMapper.deleteById(id);
        return CommonResult.success();
    }
}
