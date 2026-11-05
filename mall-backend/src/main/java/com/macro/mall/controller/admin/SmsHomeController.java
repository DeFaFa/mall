package com.macro.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macro.mall.common.CommonPage;
import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.*;
import com.macro.mall.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/home")
@RequiredArgsConstructor
public class SmsHomeController {
    private final HomeAdvertiseMapper advertiseMapper;
    private final HomeBrandMapper homeBrandMapper;
    private final HomeNewProductMapper homeNewProductMapper;
    private final HomeRecommendProductMapper homeRecommendProductMapper;
    private final BrandMapper brandMapper;
    private final ProductMapper productMapper;

    // ---------------- 广告位 ----------------
    @GetMapping("/advertise/list")
    public CommonResult<CommonPage<HomeAdvertise>> advertiseList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type) {
        return CommonResult.success(CommonPage.of(advertiseMapper.selectPage(
                new Page<>(pageNum, pageSize), new LambdaQueryWrapper<HomeAdvertise>()
                        .like(keyword != null && !keyword.isBlank(), HomeAdvertise::getName, keyword)
                        .eq(type != null, HomeAdvertise::getType, type)
                        .orderByAsc(HomeAdvertise::getSort))));
    }

    @PostMapping("/advertise/create")
    public CommonResult<Void> createAdvertise(@RequestBody HomeAdvertise advertise) {
        if (advertise.getStatus() == null) {
            advertise.setStatus(1);
        }
        advertiseMapper.insert(advertise);
        return CommonResult.success();
    }

    @PostMapping("/advertise/update")
    public CommonResult<Void> updateAdvertise(@RequestBody HomeAdvertise advertise) {
        advertiseMapper.updateById(advertise);
        return CommonResult.success();
    }

    @PostMapping("/advertise/delete")
    public CommonResult<Void> deleteAdvertise(@RequestBody List<Long> ids) {
        advertiseMapper.deleteBatchIds(ids);
        return CommonResult.success();
    }

    // ---------------- 品牌推荐 ----------------
    @GetMapping("/brand/list")
    public CommonResult<CommonPage<HomeBrand>> brandList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return CommonResult.success(CommonPage.of(homeBrandMapper.selectPage(
                new Page<>(pageNum, pageSize), new LambdaQueryWrapper<HomeBrand>()
                        .like(keyword != null && !keyword.isBlank(), HomeBrand::getBrandName, keyword)
                        .eq(HomeBrand::getRecommendStatus, 1)
                        .orderByAsc(HomeBrand::getSort))));
    }

    @PostMapping("/brand/create")
    public CommonResult<Void> createBrand(@RequestBody HomeBrand homeBrand) {
        if (homeBrand.getBrandId() == null) {
            return CommonResult.failed("请选择品牌");
        }
        long exists = homeBrandMapper.selectCount(new LambdaQueryWrapper<HomeBrand>()
                .eq(HomeBrand::getBrandId, homeBrand.getBrandId()));
        if (exists > 0) {
            return CommonResult.failed("该品牌已在推荐位");
        }
        Brand brand = brandMapper.selectById(homeBrand.getBrandId());
        homeBrand.setBrandName(brand == null ? "" : brand.getName());
        if (homeBrand.getRecommendStatus() == null) {
            homeBrand.setRecommendStatus(1);
        }
        homeBrandMapper.insert(homeBrand);
        return CommonResult.success();
    }

    @PostMapping("/brand/update")
    public CommonResult<Void> updateBrand(@RequestBody HomeBrand homeBrand) {
        homeBrandMapper.updateById(homeBrand);
        return CommonResult.success();
    }

    @PostMapping("/brand/delete")
    public CommonResult<Void> deleteBrand(@RequestBody List<Long> ids) {
        homeBrandMapper.deleteBatchIds(ids);
        return CommonResult.success();
    }

    // ---------------- 新品推荐 ----------------
    @GetMapping("/newProduct/list")
    public CommonResult<CommonPage<HomeNewProduct>> newProductList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return CommonResult.success(CommonPage.of(homeNewProductMapper.selectPage(
                new Page<>(pageNum, pageSize), new LambdaQueryWrapper<HomeNewProduct>()
                        .like(keyword != null && !keyword.isBlank(), HomeNewProduct::getProductName, keyword)
                        .eq(HomeNewProduct::getRecommendStatus, 1)
                        .orderByAsc(HomeNewProduct::getSort))));
    }

    @PostMapping("/newProduct/create")
    public CommonResult<Void> createNewProduct(@RequestBody HomeNewProduct item) {
        if (item.getProductId() == null) {
            return CommonResult.failed("请选择商品");
        }
        long exists = homeNewProductMapper.selectCount(new LambdaQueryWrapper<HomeNewProduct>()
                .eq(HomeNewProduct::getProductId, item.getProductId()));
        if (exists > 0) {
            return CommonResult.failed("该商品已在新品推荐位");
        }
        Product product = productMapper.selectById(item.getProductId());
        item.setProductName(product == null ? "" : product.getName());
        if (item.getRecommendStatus() == null) {
            item.setRecommendStatus(1);
        }
        homeNewProductMapper.insert(item);
        return CommonResult.success();
    }

    @PostMapping("/newProduct/update")
    public CommonResult<Void> updateNewProduct(@RequestBody HomeNewProduct item) {
        homeNewProductMapper.updateById(item);
        return CommonResult.success();
    }

    @PostMapping("/newProduct/delete")
    public CommonResult<Void> deleteNewProduct(@RequestBody List<Long> ids) {
        homeNewProductMapper.deleteBatchIds(ids);
        return CommonResult.success();
    }

    // ---------------- 人气推荐 ----------------
    @GetMapping("/recommendProduct/list")
    public CommonResult<CommonPage<HomeRecommendProduct>> recommendProductList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return CommonResult.success(CommonPage.of(homeRecommendProductMapper.selectPage(
                new Page<>(pageNum, pageSize), new LambdaQueryWrapper<HomeRecommendProduct>()
                        .like(keyword != null && !keyword.isBlank(), HomeRecommendProduct::getProductName, keyword)
                        .eq(HomeRecommendProduct::getRecommendStatus, 1)
                        .orderByAsc(HomeRecommendProduct::getSort))));
    }

    @PostMapping("/recommendProduct/create")
    public CommonResult<Void> createRecommendProduct(@RequestBody HomeRecommendProduct item) {
        if (item.getProductId() == null) {
            return CommonResult.failed("请选择商品");
        }
        long exists = homeRecommendProductMapper.selectCount(new LambdaQueryWrapper<HomeRecommendProduct>()
                .eq(HomeRecommendProduct::getProductId, item.getProductId()));
        if (exists > 0) {
            return CommonResult.failed("该商品已在人气推荐位");
        }
        Product product = productMapper.selectById(item.getProductId());
        item.setProductName(product == null ? "" : product.getName());
        if (item.getRecommendStatus() == null) {
            item.setRecommendStatus(1);
        }
        homeRecommendProductMapper.insert(item);
        return CommonResult.success();
    }

    @PostMapping("/recommendProduct/update")
    public CommonResult<Void> updateRecommendProduct(@RequestBody HomeRecommendProduct item) {
        homeRecommendProductMapper.updateById(item);
        return CommonResult.success();
    }

    @PostMapping("/recommendProduct/delete")
    public CommonResult<Void> deleteRecommendProduct(@RequestBody List<Long> ids) {
        homeRecommendProductMapper.deleteBatchIds(ids);
        return CommonResult.success();
    }
}
