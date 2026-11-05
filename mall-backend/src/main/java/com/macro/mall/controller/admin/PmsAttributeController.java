package com.macro.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.ProductAttribute;
import com.macro.mall.entity.ProductAttributeCategory;
import com.macro.mall.mapper.ProductAttributeCategoryMapper;
import com.macro.mall.mapper.ProductAttributeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/productAttribute")
@RequiredArgsConstructor
public class PmsAttributeController {
    private final ProductAttributeCategoryMapper categoryMapper;
    private final ProductAttributeMapper attributeMapper;

    @GetMapping("/category/list")
    public CommonResult<List<ProductAttributeCategory>> categoryList() {
        return CommonResult.success(categoryMapper.selectList(new LambdaQueryWrapper<ProductAttributeCategory>()
                .orderByAsc(ProductAttributeCategory::getId)));
    }

    @PostMapping("/category/create")
    public CommonResult<Void> createCategory(@RequestBody ProductAttributeCategory category) {
        if (category.getName() == null || category.getName().isBlank()) {
            return CommonResult.failed("属性分类名称不能为空");
        }
        category.setCreateTime(LocalDateTime.now());
        categoryMapper.insert(category);
        return CommonResult.success();
    }

    @PostMapping("/category/update")
    public CommonResult<Void> updateCategory(@RequestBody ProductAttributeCategory category) {
        categoryMapper.updateById(category);
        return CommonResult.success();
    }

    @PostMapping("/category/delete/{id}")
    public CommonResult<Void> deleteCategory(@PathVariable Long id) {
        long count = attributeMapper.selectCount(new LambdaQueryWrapper<ProductAttribute>()
                .eq(ProductAttribute::getProductAttributeCategoryId, id));
        if (count > 0) {
            return CommonResult.failed("请先删除该分类下的属性");
        }
        categoryMapper.deleteById(id);
        return CommonResult.success();
    }

    @GetMapping("/list")
    public CommonResult<List<ProductAttribute>> list(@RequestParam(required = false) Long categoryId,
                                                     @RequestParam(required = false) Integer type) {
        return CommonResult.success(attributeMapper.selectList(new LambdaQueryWrapper<ProductAttribute>()
                .eq(categoryId != null, ProductAttribute::getProductAttributeCategoryId, categoryId)
                .eq(type != null, ProductAttribute::getType, type)
                .orderByAsc(ProductAttribute::getSort)));
    }

    @PostMapping("/create")
    public CommonResult<Void> create(@RequestBody ProductAttribute attribute) {
        if (attribute.getName() == null || attribute.getName().isBlank()) {
            return CommonResult.failed("属性名称不能为空");
        }
        attributeMapper.insert(attribute);
        return CommonResult.success();
    }

    @PostMapping("/update")
    public CommonResult<Void> update(@RequestBody ProductAttribute attribute) {
        attributeMapper.updateById(attribute);
        return CommonResult.success();
    }

    @PostMapping("/delete/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        attributeMapper.deleteById(id);
        return CommonResult.success();
    }
}
