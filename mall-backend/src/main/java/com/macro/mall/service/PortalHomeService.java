package com.macro.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macro.mall.dto.FlashProductView;
import com.macro.mall.dto.HomeResult;
import com.macro.mall.dto.PrefrenceAreaView;
import com.macro.mall.entity.*;
import com.macro.mall.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortalHomeService {
    private final HomeAdvertiseMapper advertiseMapper;
    private final HomeBrandMapper homeBrandMapper;
    private final HomeNewProductMapper homeNewProductMapper;
    private final HomeRecommendProductMapper homeRecommendProductMapper;
    private final BrandMapper brandMapper;
    private final ProductMapper productMapper;
    private final SubjectMapper subjectMapper;
    private final PrefrenceAreaMapper prefrenceAreaMapper;
    private final FlashPromotionMapper flashPromotionMapper;
    private final FlashPromotionSessionMapper flashSessionMapper;
    private final FlashPromotionProductRelationMapper flashRelationMapper;
    private final PortalProductService portalProductService;
    private final PriceService priceService;

    public HomeResult index() {
        HomeResult result = new HomeResult();
        LocalDateTime now = LocalDateTime.now();

        List<HomeAdvertise> ads = advertiseMapper.selectList(new LambdaQueryWrapper<HomeAdvertise>()
                .eq(HomeAdvertise::getStatus, 1)
                .le(HomeAdvertise::getStartTime, now)
                .ge(HomeAdvertise::getEndTime, now)
                .orderByAsc(HomeAdvertise::getSort));
        result.setBanners(ads.stream().filter(a -> a.getType() == 0).toList());
        result.setRecommends(ads.stream().filter(a -> a.getType() == 1).toList());

        List<Long> brandIds = homeBrandMapper.selectList(new LambdaQueryWrapper<HomeBrand>()
                        .eq(HomeBrand::getRecommendStatus, 1).orderByAsc(HomeBrand::getSort))
                .stream().map(HomeBrand::getBrandId).toList();
        result.setBrands(orderedBrands(brandIds));

        List<Long> newIds = homeNewProductMapper.selectList(new LambdaQueryWrapper<HomeNewProduct>()
                        .eq(HomeNewProduct::getRecommendStatus, 1).orderByAsc(HomeNewProduct::getSort))
                .stream().map(HomeNewProduct::getProductId).toList();
        result.setNewProducts(portalProductService.productsByIds(newIds));

        List<Long> hotIds = homeRecommendProductMapper.selectList(new LambdaQueryWrapper<HomeRecommendProduct>()
                        .eq(HomeRecommendProduct::getRecommendStatus, 1).orderByAsc(HomeRecommendProduct::getSort))
                .stream().map(HomeRecommendProduct::getProductId).toList();
        result.setHotProducts(portalProductService.productsByIds(hotIds));

        fillFlash(result);

        result.setSubjects(subjectMapper.selectList(new LambdaQueryWrapper<Subject>()
                .eq(Subject::getShowStatus, 1).orderByAsc(Subject::getSort).last("limit 4")));

        List<PrefrenceArea> areas = prefrenceAreaMapper.selectList(new LambdaQueryWrapper<PrefrenceArea>()
                .eq(PrefrenceArea::getShowStatus, 1).orderByAsc(PrefrenceArea::getSort));
        List<PrefrenceAreaView> areaViews = new ArrayList<>();
        for (PrefrenceArea area : areas) {
            PrefrenceAreaView view = new PrefrenceAreaView();
            view.setArea(area);
            view.setProducts(portalProductService.prefrenceProducts(area.getId()));
            areaViews.add(view);
        }
        result.setPrefrenceAreas(areaViews);
        result.setCategories(portalProductService.categoryTree());
        return result;
    }

    private void fillFlash(HomeResult result) {
        LocalDate today = LocalDate.now();
        FlashPromotion promotion = flashPromotionMapper.selectOne(new LambdaQueryWrapper<FlashPromotion>()
                .eq(FlashPromotion::getStatus, 1)
                .le(FlashPromotion::getStartDate, today)
                .ge(FlashPromotion::getEndDate, today)
                .orderByDesc(FlashPromotion::getId)
                .last("limit 1"));
        if (promotion == null) {
            result.setFlashProducts(List.of());
            return;
        }
        FlashPromotionSession session = priceService.currentSession();
        result.setFlashPromotion(promotion);
        result.setFlashSession(session);
        if (session == null) {
            result.setFlashProducts(List.of());
            return;
        }
        List<FlashPromotionProductRelation> relations = flashRelationMapper.selectList(
                new LambdaQueryWrapper<FlashPromotionProductRelation>()
                        .eq(FlashPromotionProductRelation::getFlashPromotionId, promotion.getId())
                        .eq(FlashPromotionProductRelation::getFlashPromotionSessionId, session.getId())
                        .orderByAsc(FlashPromotionProductRelation::getSort));
        List<Long> productIds = relations.stream().map(FlashPromotionProductRelation::getProductId).toList();
        Map<Long, Product> products = productMapper.selectBatchIds(productIds).stream()
                .filter(p -> p.getDeleteStatus() == 0 && p.getPublishStatus() == 1)
                .collect(Collectors.toMap(Product::getId, p -> p));
        List<FlashProductView> views = new ArrayList<>();
        for (FlashPromotionProductRelation relation : relations) {
            Product product = products.get(relation.getProductId());
            if (product == null) {
                continue;
            }
            FlashProductView view = new FlashProductView();
            view.setProduct(product);
            view.setFlashPrice(relation.getFlashPromotionPrice());
            view.setFlashCount(relation.getFlashPromotionCount());
            view.setFlashLimit(relation.getFlashPromotionLimit());
            view.setSessionId(relation.getFlashPromotionSessionId());
            views.add(view);
        }
        result.setFlashProducts(views);
    }

    private List<Brand> orderedBrands(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Brand> map = brandMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Brand::getId, b -> b));
        List<Brand> brands = new ArrayList<>();
        for (Long id : ids) {
            Brand brand = map.get(id);
            if (brand != null) {
                brands.add(brand);
            }
        }
        return brands;
    }
}
