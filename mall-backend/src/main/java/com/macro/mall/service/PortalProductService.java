package com.macro.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macro.mall.common.BizException;
import com.macro.mall.dto.ProductDetail;
import com.macro.mall.entity.*;
import com.macro.mall.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortalProductService {
    private final ProductMapper productMapper;
    private final ProductCategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final SubjectMapper subjectMapper;
    private final LinkMapper linkMapper;
    private final MemberMapper memberMapper;
    private final ProductService productService;
    private final PriceService priceService;

    public Page<Product> search(int pageNum, int pageSize, String keyword, Long brandId, Long categoryId,
                                String sort, Integer priceMin, Integer priceMax) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleteStatus, 0)
                .eq(Product::getPublishStatus, 1)
                .eq(Product::getVerifyStatus, 1);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Product::getName, keyword)
                    .or().like(Product::getSubTitle, keyword)
                    .or().like(Product::getKeywords, keyword));
        }
        if (brandId != null) {
            wrapper.eq(Product::getBrandId, brandId);
        }
        if (categoryId != null) {
            List<Long> ids = new ArrayList<>();
            ids.add(categoryId);
            List<ProductCategory> children = categoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                    .eq(ProductCategory::getParentId, categoryId));
            children.forEach(c -> ids.add(c.getId()));
            wrapper.in(Product::getProductCategoryId, ids);
        }
        if (priceMin != null) {
            wrapper.ge(Product::getPrice, priceMin);
        }
        if (priceMax != null) {
            wrapper.le(Product::getPrice, priceMax);
        }
        switch (sort == null ? "" : sort) {
            case "price_asc" -> wrapper.orderByAsc(Product::getPrice);
            case "price_desc" -> wrapper.orderByDesc(Product::getPrice);
            case "sale_desc" -> wrapper.orderByDesc(Product::getSale);
            default -> wrapper.orderByDesc(Product::getId);
        }
        return productMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public ProductDetail detail(Long productId, Long memberId) {
        ProductDetail detail = productService.detail(productId);
        Product product = detail.getProduct();
        if (product.getDeleteStatus() == 1 || product.getPublishStatus() != 1) {
            throw new BizException("商品已下架");
        }
        if (memberId != null) {
            Member member = memberMapper.selectById(memberId);
            Long levelId = member == null ? null : member.getMemberLevelId();
            if (!detail.getSkuStockList().isEmpty()) {
                detail.setCurrentPrice(priceService.unitPrice(product, detail.getSkuStockList().get(0), 1, levelId));
            }
        }
        return detail;
    }

    public List<ProductCategory> categoryTree() {
        List<ProductCategory> all = categoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getShowStatus, 1)
                .orderByAsc(ProductCategory::getSort));
        Map<Long, ProductCategory> map = all.stream().collect(Collectors.toMap(ProductCategory::getId, c -> {
            c.setChildren(new ArrayList<>());
            return c;
        }));
        List<ProductCategory> roots = new ArrayList<>();
        for (ProductCategory category : all) {
            ProductCategory parent = map.get(category.getParentId());
            if (category.getParentId() == null || category.getParentId() == 0 || parent == null) {
                roots.add(category);
            } else {
                parent.getChildren().add(category);
            }
        }
        return roots;
    }

    public List<Brand> brands() {
        return brandMapper.selectList(new LambdaQueryWrapper<Brand>()
                .eq(Brand::getShowStatus, 1).orderByAsc(Brand::getSort));
    }

    public List<Subject> subjects() {
        return subjectMapper.selectList(new LambdaQueryWrapper<Subject>()
                .eq(Subject::getShowStatus, 1).orderByAsc(Subject::getSort));
    }

    public List<Product> productsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Product> map = productMapper.selectBatchIds(ids).stream()
                .filter(p -> p.getDeleteStatus() == 0 && p.getPublishStatus() == 1)
                .collect(Collectors.toMap(Product::getId, p -> p));
        List<Product> ordered = new ArrayList<>();
        for (Long id : ids) {
            Product product = map.get(id);
            if (product != null) {
                ordered.add(product);
            }
        }
        return ordered;
    }

    public List<Product> subjectProducts(Long subjectId) {
        return productsByIds(linkMapper.selectProductIdsBySubject(subjectId));
    }

    public List<Product> prefrenceProducts(Long areaId) {
        return productsByIds(linkMapper.selectProductIdsByPrefrenceArea(areaId));
    }
}
