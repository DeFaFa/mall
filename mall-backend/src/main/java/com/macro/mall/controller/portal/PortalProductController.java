package com.macro.mall.controller.portal;

import com.macro.mall.auth.AuthContext;
import com.macro.mall.common.CommonPage;
import com.macro.mall.common.CommonResult;
import com.macro.mall.dto.ProductDetail;
import com.macro.mall.entity.Brand;
import com.macro.mall.entity.Coupon;
import com.macro.mall.entity.Product;
import com.macro.mall.entity.ProductCategory;
import com.macro.mall.entity.Subject;
import com.macro.mall.service.CouponService;
import com.macro.mall.service.MemberService;
import com.macro.mall.service.PortalProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portal/product")
@RequiredArgsConstructor
public class PortalProductController {
    private final PortalProductService portalProductService;
    private final CouponService couponService;
    private final MemberService memberService;

    @GetMapping("/search")
    public CommonResult<CommonPage<Product>> search(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "12") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer priceMin,
            @RequestParam(required = false) Integer priceMax) {
        return CommonResult.success(CommonPage.of(portalProductService.search(
                pageNum, pageSize, keyword, brandId, categoryId, sort, priceMin, priceMax)));
    }

    @GetMapping("/{id}")
    public CommonResult<ProductDetail> detail(@PathVariable Long id) {
        Long memberId = AuthContext.getMemberId();
        ProductDetail detail = portalProductService.detail(id, memberId);
        if (memberId != null) {
            memberService.recordHistory(memberId, id);
        }
        return CommonResult.success(detail);
    }

    @GetMapping("/{id}/coupons")
    public CommonResult<List<Coupon>> coupons(@PathVariable Long id) {
        ProductDetail detail = portalProductService.detail(id, null);
        return CommonResult.success(couponService.couponsForProduct(id,
                detail.getProduct().getProductCategoryId()));
    }

    @GetMapping("/category/tree")
    public CommonResult<List<ProductCategory>> categories() {
        return CommonResult.success(portalProductService.categoryTree());
    }

    @GetMapping("/brand/list")
    public CommonResult<List<Brand>> brands() {
        return CommonResult.success(portalProductService.brands());
    }

    @GetMapping("/subject/list")
    public CommonResult<List<Subject>> subjects() {
        return CommonResult.success(portalProductService.subjects());
    }

    @GetMapping("/subject/{id}/products")
    public CommonResult<List<Product>> subjectProducts(@PathVariable Long id) {
        return CommonResult.success(portalProductService.subjectProducts(id));
    }
}
