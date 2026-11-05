package com.macro.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macro.mall.common.CommonPage;
import com.macro.mall.common.CommonResult;
import com.macro.mall.dto.CouponParam;
import com.macro.mall.entity.*;
import com.macro.mall.mapper.*;
import com.macro.mall.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/coupon")
@RequiredArgsConstructor
public class SmsCouponController {
    private final CouponMapper couponMapper;
    private final ProductMapper productMapper;
    private final ProductCategoryMapper categoryMapper;
    private final LinkMapper linkMapper;
    private final CouponService couponService;

    @GetMapping("/list")
    public CommonResult<CommonPage<Coupon>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return CommonResult.success(CommonPage.of(couponMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Coupon>()
                        .like(keyword != null && !keyword.isBlank(), Coupon::getName, keyword)
                        .orderByDesc(Coupon::getId))));
    }

    @GetMapping("/{id}")
    public CommonResult<CouponParam> detail(@PathVariable Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            return CommonResult.failed("优惠券不存在");
        }
        CouponParam param = new CouponParam();
        BeanUtils.copyProperties(coupon, param);
        param.setProductIds(linkMapper.selectProductIdsByCoupon(id));
        param.setCategoryIds(linkMapper.selectCategoryIdsByCoupon(id));
        return CommonResult.success(param);
    }

    @PostMapping("/create")
    @Transactional
    public CommonResult<Long> create(@RequestBody CouponParam param) {
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(param, coupon);
        coupon.setId(null);
        coupon.setCreateTime(LocalDateTime.now());
        if (coupon.getUseType() == null) {
            coupon.setUseType(0);
        }
        couponMapper.insert(coupon);
        saveRelations(coupon.getId(), param);
        return CommonResult.success(coupon.getId());
    }

    @PostMapping("/update/{id}")
    @Transactional
    public CommonResult<Void> update(@PathVariable Long id, @RequestBody CouponParam param) {
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(param, coupon);
        coupon.setId(id);
        couponMapper.updateById(coupon);
        linkMapper.deleteCouponProducts(id);
        linkMapper.deleteCouponCategories(id);
        saveRelations(id, param);
        return CommonResult.success();
    }

    private void saveRelations(Long couponId, CouponParam param) {
        if (param.getProductIds() != null) {
            for (Long productId : param.getProductIds()) {
                Product product = productMapper.selectById(productId);
                linkMapper.insertCouponProduct(couponId, productId,
                        product == null ? "" : product.getName(),
                        product == null ? "" : product.getProductSn());
            }
        }
        if (param.getCategoryIds() != null) {
            for (Long categoryId : param.getCategoryIds()) {
                ProductCategory category = categoryMapper.selectById(categoryId);
                ProductCategory parent = category == null || category.getParentId() == 0 ? null
                        : categoryMapper.selectById(category.getParentId());
                linkMapper.insertCouponCategory(couponId, categoryId,
                        category == null ? "" : category.getName(),
                        parent == null ? "" : parent.getName(),
                        parent == null ? 0L : parent.getId());
            }
        }
    }

    @PostMapping("/delete")
    @Transactional
    public CommonResult<Void> delete(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            linkMapper.deleteCouponProducts(id);
            linkMapper.deleteCouponCategories(id);
            couponMapper.deleteById(id);
        }
        return CommonResult.success();
    }

    @GetMapping("/history")
    public CommonResult<List<CouponHistory>> history(@RequestParam Long couponId) {
        return CommonResult.success(couponService.historyOfCoupon(couponId));
    }
}
