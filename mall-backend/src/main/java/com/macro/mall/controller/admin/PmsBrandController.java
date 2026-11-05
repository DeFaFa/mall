package com.macro.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macro.mall.common.CommonPage;
import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.Brand;
import com.macro.mall.mapper.BrandMapper;
import com.macro.mall.mapper.LinkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/brand")
@RequiredArgsConstructor
public class PmsBrandController {
    private final BrandMapper brandMapper;
    private final LinkMapper linkMapper;

    @GetMapping("/list")
    public CommonResult<CommonPage<Brand>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer showStatus,
            @RequestParam(required = false) Integer factoryStatus) {
        Page<Brand> page = brandMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<Brand>()
                .like(keyword != null && !keyword.isBlank(), Brand::getName, keyword)
                .eq(showStatus != null, Brand::getShowStatus, showStatus)
                .eq(factoryStatus != null, Brand::getFactoryStatus, factoryStatus)
                .orderByAsc(Brand::getSort));
        return CommonResult.success(CommonPage.of(page));
    }

    @GetMapping("/all")
    public CommonResult<List<Brand>> all() {
        return CommonResult.success(brandMapper.selectList(new LambdaQueryWrapper<Brand>()
                .orderByAsc(Brand::getSort)));
    }

    @PostMapping("/create")
    public CommonResult<Void> create(@RequestBody Brand brand) {
        if (brand.getName() == null || brand.getName().isBlank()) {
            return CommonResult.failed("品牌名称不能为空");
        }
        brand.setCreateTime(LocalDateTime.now());
        if (brand.getShowStatus() == null) {
            brand.setShowStatus(1);
            brand.setFactoryStatus(0);
        }
        brandMapper.insert(brand);
        return CommonResult.success();
    }

    @PostMapping("/update")
    public CommonResult<Void> update(@RequestBody Brand brand) {
        brandMapper.updateById(brand);
        return CommonResult.success();
    }

    @PostMapping("/delete")
    public CommonResult<Void> delete(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            linkMapper.deleteHomeBrandByBrand(id);
            brandMapper.deleteById(id);
        }
        return CommonResult.success();
    }

    @PostMapping("/batchStatus")
    public CommonResult<Void> batchStatus(@RequestParam List<Long> ids,
                                          @RequestParam String field,
                                          @RequestParam Integer value) {
        List<Brand> brands = brandMapper.selectBatchIds(ids);
        for (Brand brand : brands) {
            if ("showStatus".equals(field)) {
                brand.setShowStatus(value);
            } else if ("factoryStatus".equals(field)) {
                brand.setFactoryStatus(value);
            }
            brandMapper.updateById(brand);
        }
        return CommonResult.success();
    }
}
