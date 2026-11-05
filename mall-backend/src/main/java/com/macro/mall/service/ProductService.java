package com.macro.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macro.mall.common.BizException;
import com.macro.mall.dto.ProductDetail;
import com.macro.mall.dto.ProductParam;
import com.macro.mall.entity.*;
import com.macro.mall.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductMapper productMapper;
    private final SkuStockMapper skuStockMapper;
    private final ProductAttributeValueMapper attributeValueMapper;
    private final ProductAttributeMapper attributeMapper;
    private final ProductAttributeCategoryMapper attributeCategoryMapper;
    private final MemberPriceMapper memberPriceMapper;
    private final ProductLadderMapper ladderMapper;
    private final ProductFullReductionMapper fullReductionMapper;
    private final ProductCategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final CartItemMapper cartItemMapper;
    private final LinkMapper linkMapper;
    private final SubjectMapper subjectMapper;
    private final PrefrenceAreaMapper prefrenceAreaMapper;
    private final PriceService priceService;

    public Page<Product> page(int pageNum, int pageSize, String keyword, String productSn, Long brandId,
                              Long categoryId, Integer publishStatus, Integer verifyStatus, Integer deleteStatus) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Product::getName, keyword).or().like(Product::getProductSn, keyword));
        }
        wrapper.like(productSn != null && !productSn.isBlank(), Product::getProductSn, productSn)
                .eq(brandId != null, Product::getBrandId, brandId)
                .eq(categoryId != null, Product::getProductCategoryId, categoryId)
                .eq(publishStatus != null, Product::getPublishStatus, publishStatus)
                .eq(verifyStatus != null, Product::getVerifyStatus, verifyStatus)
                .eq(deleteStatus != null, Product::getDeleteStatus, deleteStatus)
                .orderByDesc(Product::getId);
        return productMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public ProductDetail detail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException("商品不存在");
        }
        ProductDetail detail = new ProductDetail();
        detail.setProduct(product);
        ProductCategory category = categoryMapper.selectById(product.getProductCategoryId());
        detail.setCategoryName(category == null ? "" : category.getName());
        Brand brand = brandMapper.selectById(product.getBrandId());
        detail.setBrandName(brand == null ? "" : brand.getName());
        detail.setSkuStockList(skuStockMapper.selectList(new LambdaQueryWrapper<SkuStock>()
                .eq(SkuStock::getProductId, id)));
        detail.setAttributeValueList(attributeValueMapper.selectList(new LambdaQueryWrapper<ProductAttributeValue>()
                .eq(ProductAttributeValue::getProductId, id)));
        detail.setMemberPriceList(memberPriceMapper.selectList(new LambdaQueryWrapper<MemberPrice>()
                .eq(MemberPrice::getProductId, id)));
        detail.setLadderList(ladderMapper.selectList(new LambdaQueryWrapper<ProductLadder>()
                .eq(ProductLadder::getProductId, id)));
        detail.setFullReductionList(fullReductionMapper.selectList(new LambdaQueryWrapper<ProductFullReduction>()
                .eq(ProductFullReduction::getProductId, id)));
        detail.setSubjectIds(linkMapper.selectSubjectIdsByProduct(id));
        detail.setPrefrenceAreaIds(linkMapper.selectPrefrenceAreaIdsByProduct(id));
        ProductAttributeCategory attributeCategory = attributeCategoryMapper.selectOne(
                new LambdaQueryWrapper<ProductAttributeCategory>()
                        .eq(ProductAttributeCategory::getProductCategoryId, product.getProductCategoryId())
                        .last("limit 1"));
        if (attributeCategory != null) {
            detail.setParamList(attributeMapper.selectList(new LambdaQueryWrapper<ProductAttribute>()
                    .eq(ProductAttribute::getProductAttributeCategoryId, attributeCategory.getId())
                    .eq(ProductAttribute::getType, 1)));
        } else {
            detail.setParamList(List.of());
        }
        if (detail.getSkuStockList().isEmpty()) {
            throw new BizException("商品没有可售 SKU");
        }
        detail.setCurrentPrice(priceService.unitPrice(product, detail.getSkuStockList().get(0), 1, null));
        detail.setFlashPrice(priceService.activeFlashPrice(id));
        return detail;
    }

    @Transactional
    public Long create(ProductParam param) {
        validate(param);
        Product product = new Product();
        BeanUtils.copyProperties(param, product);
        product.setId(null);
        product.setDeleteStatus(0);
        product.setSale(0);
        product.setStock(0);
        product.setVerifyStatus(param.getVerifyStatus() == null ? 0 : param.getVerifyStatus());
        product.setPublishStatus(param.getPublishStatus() == null ? 1 : param.getPublishStatus());
        product.setNewStatus(param.getNewStatus() == null ? 1 : param.getNewStatus());
        product.setRecommandStatus(param.getRecommandStatus() == null ? 0 : param.getRecommandStatus());
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        if (product.getProductSn() == null || product.getProductSn().isBlank()) {
            product.setProductSn("P" + System.currentTimeMillis());
        }
        productMapper.insert(product);
        saveChildTables(product.getId(), param);
        saveRelations(product.getId(), param);
        refreshProductStock(product.getId());
        return product.getId();
    }

    @Transactional
    public void update(Long id, ProductParam param) {
        Product existing = productMapper.selectById(id);
        if (existing == null || existing.getDeleteStatus() == 1) {
            throw new BizException("商品不存在");
        }
        validate(param);
        Product product = new Product();
        BeanUtils.copyProperties(param, product);
        product.setId(id);
        product.setCreateTime(existing.getCreateTime());
        product.setDeleteStatus(existing.getDeleteStatus());
        product.setSale(existing.getSale());
        product.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(product);

        linkMapper.deleteAttributeValues(id);
        linkMapper.deleteMemberPrices(id);
        linkMapper.deleteLadders(id);
        linkMapper.deleteFullReductions(id);
        saveChildTables(id, param);
        linkMapper.deleteSubjectProductsByProduct(id);
        linkMapper.deletePrefrenceProductsByProduct(id);
        // 商品与专题/优选专区的关系按商品维度重建
        saveRelations(id, param);
        refreshProductStock(id);
    }

    private void validate(ProductParam param) {
        if (param.getName() == null || param.getName().isBlank()) {
            throw new BizException("商品名称不能为空");
        }
        if (param.getProductCategoryId() == null) {
            throw new BizException("请选择商品分类");
        }
        if (param.getSkuStockList() == null || param.getSkuStockList().isEmpty()) {
            throw new BizException("至少需要一个 SKU");
        }
    }

    private void saveChildTables(Long productId, ProductParam param) {
        if (param.getProductAttributeValueList() != null) {
            for (ProductAttributeValue value : param.getProductAttributeValueList()) {
                value.setId(null);
                value.setProductId(productId);
                if (value.getProductAttributeId() != null && value.getValue() != null && !value.getValue().isBlank()) {
                    attributeValueMapper.insert(value);
                }
            }
        }
        if (param.getMemberPriceList() != null) {
            for (MemberPrice price : param.getMemberPriceList()) {
                price.setId(null);
                price.setProductId(productId);
                memberPriceMapper.insert(price);
            }
        }
        if (param.getProductLadderList() != null) {
            for (ProductLadder ladder : param.getProductLadderList()) {
                ladder.setId(null);
                ladder.setProductId(productId);
                ladderMapper.insert(ladder);
            }
        }
        if (param.getProductFullReductionList() != null) {
            for (ProductFullReduction reduction : param.getProductFullReductionList()) {
                reduction.setId(null);
                reduction.setProductId(productId);
                fullReductionMapper.insert(reduction);
            }
        }
        saveSkus(productId, param.getSkuStockList());
    }

    private void saveSkus(Long productId, List<SkuStock> incoming) {
        List<SkuStock> existing = skuStockMapper.selectList(new LambdaQueryWrapper<SkuStock>()
                .eq(SkuStock::getProductId, productId));
        Map<Long, SkuStock> existingMap = existing.stream()
                .collect(Collectors.toMap(SkuStock::getId, s -> s));
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Set<Long> keepIds = new HashSet<>();
        int index = 0;
        for (SkuStock sku : incoming) {
            index++;
            SkuStock target;
            if (sku.getId() != null && existingMap.containsKey(sku.getId())) {
                target = existingMap.get(sku.getId());
                target.setPrice(sku.getPrice());
                target.setStock(sku.getStock());
                target.setLowStock(sku.getLowStock() == null ? 0 : sku.getLowStock());
                target.setPic(sku.getPic());
                target.setSpData(sku.getSpData() == null ? "{}" : sku.getSpData());
                skuStockMapper.updateById(target);
                keepIds.add(target.getId());
            } else {
                target = new SkuStock();
                target.setProductId(productId);
                target.setSkuCode(String.format("%s%04d%03d", date, productId, index));
                target.setPrice(sku.getPrice());
                target.setStock(sku.getStock() == null ? 0 : sku.getStock());
                target.setLockStock(0);
                target.setLowStock(sku.getLowStock() == null ? 0 : sku.getLowStock());
                target.setPic(sku.getPic());
                target.setSale(0);
                target.setSpData(sku.getSpData() == null ? "{}" : sku.getSpData());
                skuStockMapper.insert(target);
                keepIds.add(target.getId());
            }
        }
        for (SkuStock old : existing) {
            if (!keepIds.contains(old.getId())) {
                long inCart = cartItemMapper.selectCount(new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getSkuId, old.getId()));
                if (inCart == 0) {
                    skuStockMapper.deleteById(old.getId());
                }
            }
        }
    }

    private void saveRelations(Long productId, ProductParam param) {
        if (param.getSubjectIds() != null) {
            for (Long subjectId : param.getSubjectIds()) {
                linkMapper.insertSubjectProduct(subjectId, productId);
            }
        }
        if (param.getPrefrenceAreaIds() != null) {
            for (Long areaId : param.getPrefrenceAreaIds()) {
                linkMapper.insertPrefrenceProduct(areaId, productId);
            }
        }
    }

    private void refreshProductStock(Long productId) {
        List<SkuStock> skus = skuStockMapper.selectList(new LambdaQueryWrapper<SkuStock>()
                .eq(SkuStock::getProductId, productId));
        int stock = skus.stream().mapToInt(s -> s.getStock() == null ? 0 : s.getStock()).sum();
        int sale = skus.stream().mapToInt(s -> s.getSale() == null ? 0 : s.getSale()).sum();
        Product update = new Product();
        update.setId(productId);
        update.setStock(stock);
        update.setSale(sale);
        productMapper.updateById(update);
    }

    public void batchUpdate(List<Long> ids, String field, Integer value) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException("请选择商品");
        }
        LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<Product>().in(Product::getId, ids);
        switch (field) {
            case "verifyStatus" -> wrapper.set(Product::getVerifyStatus, value);
            case "publishStatus" -> wrapper.set(Product::getPublishStatus, value);
            case "recommandStatus" -> wrapper.set(Product::getRecommandStatus, value);
            case "newStatus" -> wrapper.set(Product::getNewStatus, value);
            default -> throw new BizException("不支持的批量操作");
        }
        productMapper.update(null, wrapper);
    }

    public void logicalDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException("请选择商品");
        }
        for (Long id : ids) {
            Product update = new Product();
            update.setId(id);
            update.setDeleteStatus(1);
            productMapper.updateById(update);
            linkMapper.deleteHomeNewByProduct(id);
            linkMapper.deleteHomeRecommendByProduct(id);
            linkMapper.deleteFlashProductByProduct(id);
        }
    }

    public List<Product> simpleList(String keyword) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getDeleteStatus, 0)
                .select(Product::getId, Product::getName, Product::getPrice, Product::getPic, Product::getProductSn)
                .orderByDesc(Product::getId)
                .last("limit 50");
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Product::getName, keyword);
        }
        return productMapper.selectList(wrapper);
    }

    public Page<SkuStock> skuPage(int pageNum, int pageSize, String keyword, Long productId) {
        LambdaQueryWrapper<SkuStock> wrapper = new LambdaQueryWrapper<SkuStock>()
                .like(keyword != null && !keyword.isBlank(), SkuStock::getSkuCode, keyword)
                .eq(productId != null, SkuStock::getProductId, productId)
                .orderByDesc(SkuStock::getId);
        Page<SkuStock> page = skuStockMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Set<Long> productIds = page.getRecords().stream().map(SkuStock::getProductId).collect(Collectors.toSet());
        if (!productIds.isEmpty()) {
            Map<Long, String> names = productMapper.selectBatchIds(productIds).stream()
                    .collect(Collectors.toMap(Product::getId, Product::getName));
            page.getRecords().forEach(s -> s.setProductName(names.getOrDefault(s.getProductId(), "")));
        }
        return page;
    }

    public void updateSku(SkuStock sku) {
        if (sku.getId() == null) {
            throw new BizException("SKU 不存在");
        }
        skuStockMapper.updateById(sku);
        SkuStock saved = skuStockMapper.selectById(sku.getId());
        refreshProductStock(saved.getProductId());
    }

    public void batchStock(List<SkuStock> skus) {
        if (skus == null || skus.isEmpty()) {
            return;
        }
        Set<Long> productIds = new HashSet<>();
        for (SkuStock sku : skus) {
            skuStockMapper.updateById(sku);
            if (sku.getProductId() != null) {
                productIds.add(sku.getProductId());
            }
        }
        productIds.forEach(this::refreshProductStock);
    }

    public List<ProductCategory> categoryTree() {
        List<ProductCategory> categories = categoryMapper.selectList(new LambdaQueryWrapper<ProductCategory>()
                .orderByAsc(ProductCategory::getSort));
        Map<Long, ProductCategory> map = new LinkedHashMap<>();
        for (ProductCategory category : categories) {
            category.setChildren(new ArrayList<>());
            map.put(category.getId(), category);
        }
        List<ProductCategory> roots = new ArrayList<>();
        for (ProductCategory category : categories) {
            ProductCategory parent = map.get(category.getParentId());
            if (category.getParentId() == null || category.getParentId() == 0 || parent == null) {
                roots.add(category);
            } else {
                parent.getChildren().add(category);
            }
        }
        return roots;
    }

    public void createCategory(ProductCategory category) {
        if (category.getName() == null || category.getName().isBlank()) {
            throw new BizException("分类名称不能为空");
        }
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        category.setLevel(category.getParentId() == 0 ? 0 : 1);
        category.setProductCount(0);
        if (category.getNavStatus() == null) {
            category.setNavStatus(1);
        }
        if (category.getShowStatus() == null) {
            category.setShowStatus(1);
        }
        categoryMapper.insert(category);
    }

    public void deleteCategory(Long id) {
        long children = categoryMapper.selectCount(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getParentId, id));
        if (children > 0) {
            throw new BizException("请先删除子分类");
        }
        long products = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getProductCategoryId, id).eq(Product::getDeleteStatus, 0));
        if (products > 0) {
            throw new BizException("该分类下还有商品，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}
