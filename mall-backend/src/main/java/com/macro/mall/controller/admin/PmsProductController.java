package com.macro.mall.controller.admin;

import com.macro.mall.common.CommonPage;
import com.macro.mall.common.CommonResult;
import com.macro.mall.dto.ProductDetail;
import com.macro.mall.dto.ProductParam;
import com.macro.mall.entity.Product;
import com.macro.mall.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product")
@RequiredArgsConstructor
public class PmsProductController {
    private final ProductService productService;

    @GetMapping("/list")
    public CommonResult<CommonPage<Product>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String productSn,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long productCategoryId,
            @RequestParam(required = false) Integer publishStatus,
            @RequestParam(required = false) Integer verifyStatus,
            @RequestParam(required = false) Integer deleteStatus) {
        return CommonResult.success(CommonPage.of(productService.page(pageNum, pageSize, keyword, productSn,
                brandId, productCategoryId, publishStatus, verifyStatus, deleteStatus)));
    }

    @GetMapping("/{id}")
    public CommonResult<ProductDetail> detail(@PathVariable Long id) {
        return CommonResult.success(productService.detail(id));
    }

    @PostMapping("/create")
    public CommonResult<Long> create(@RequestBody ProductParam param) {
        return CommonResult.success(productService.create(param));
    }

    @PostMapping("/update/{id}")
    public CommonResult<Void> update(@PathVariable Long id, @RequestBody ProductParam param) {
        productService.update(id, param);
        return CommonResult.success();
    }

    @PostMapping("/delete")
    public CommonResult<Void> delete(@RequestBody List<Long> ids) {
        productService.logicalDelete(ids);
        return CommonResult.success();
    }

    @PostMapping("/batchStatus")
    public CommonResult<Void> batchStatus(@RequestParam List<Long> ids,
                                          @RequestParam String field,
                                          @RequestParam Integer value) {
        productService.batchUpdate(ids, field, value);
        return CommonResult.success();
    }

    @GetMapping("/simpleList")
    public CommonResult<List<Product>> simpleList(@RequestParam(required = false) String keyword) {
        return CommonResult.success(productService.simpleList(keyword));
    }
}
