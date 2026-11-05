package com.macro.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.PrefrenceArea;
import com.macro.mall.mapper.LinkMapper;
import com.macro.mall.mapper.PrefrenceAreaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/prefrenceArea")
@RequiredArgsConstructor
public class CmsPrefrenceAreaController {
    private final PrefrenceAreaMapper prefrenceAreaMapper;
    private final LinkMapper linkMapper;

    @GetMapping("/list")
    public CommonResult<List<PrefrenceArea>> list() {
        return CommonResult.success(prefrenceAreaMapper.selectList(new LambdaQueryWrapper<PrefrenceArea>()
                .orderByAsc(PrefrenceArea::getSort)));
    }

    @PostMapping("/create")
    public CommonResult<Void> create(@RequestBody PrefrenceArea area) {
        if (area.getName() == null || area.getName().isBlank()) {
            return CommonResult.failed("专区名称不能为空");
        }
        if (area.getShowStatus() == null) {
            area.setShowStatus(1);
        }
        prefrenceAreaMapper.insert(area);
        return CommonResult.success();
    }

    @PostMapping("/update")
    public CommonResult<Void> update(@RequestBody PrefrenceArea area) {
        prefrenceAreaMapper.updateById(area);
        return CommonResult.success();
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public CommonResult<Void> delete(@PathVariable Long id) {
        linkMapper.deletePrefrenceProducts(id);
        prefrenceAreaMapper.deleteById(id);
        return CommonResult.success();
    }
}
