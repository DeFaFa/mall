package com.macro.mall.controller.admin;

import com.macro.mall.common.CommonPage;
import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.SkuStock;
import com.macro.mall.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/sku")
@RequiredArgsConstructor
public class PmsSkuController {
    private final ProductService productService;

    @GetMapping("/list")
    public CommonResult<CommonPage<SkuStock>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String skuCode,
            @RequestParam(required = false) Long productId) {
        return CommonResult.success(CommonPage.of(productService.skuPage(pageNum, pageSize, skuCode, productId)));
    }

    @PostMapping("/update")
    public CommonResult<Void> update(@RequestBody SkuStock sku) {
        productService.updateSku(sku);
        return CommonResult.success();
    }

    @PostMapping("/batchStock")
    public CommonResult<Void> batchStock(@RequestBody List<SkuStock> skus) {
        productService.batchStock(skus);
        return CommonResult.success();
    }
}
