package com.macro.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.ProductCategory;
import com.macro.mall.mapper.ProductCategoryMapper;
import com.macro.mall.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/productCategory")
@RequiredArgsConstructor
public class PmsCategoryController {
    private final ProductCategoryMapper categoryMapper;
    private final ProductService productService;

    @GetMapping("/tree")
    public CommonResult<List<ProductCategory>> tree() {
        return CommonResult.success(productService.categoryTree());
    }

    @GetMapping("/list")
    public CommonResult<List<ProductCategory>> list() {
        return CommonResult.success(categoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                .orderByAsc(ProductCategory::getSort)));
    }

    @PostMapping("/create")
    public CommonResult<Void> create(@RequestBody ProductCategory category) {
        productService.createCategory(category);
        return CommonResult.success();
    }

    @PostMapping("/update")
    public CommonResult<Void> update(@RequestBody ProductCategory category) {
        if (category.getId() == null) {
            return CommonResult.failed("分类不存在");
        }
        if (category.getParentId() != null) {
            category.setLevel(category.getParentId() == 0 ? 0 : 1);
        }
        categoryMapper.updateById(category);
        return CommonResult.success();
    }

    @PostMapping("/delete/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        productService.deleteCategory(id);
        return CommonResult.success();
    }
}
