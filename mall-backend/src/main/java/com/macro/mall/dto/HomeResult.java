package com.macro.mall.dto;

import com.macro.mall.entity.*;
import lombok.Data;

import java.util.List;

@Data
public class HomeResult {
    private List<HomeAdvertise> banners;
    private List<HomeAdvertise> recommends;
    private List<Brand> brands;
    private List<Product> newProducts;
    private List<Product> hotProducts;
    private FlashPromotion flashPromotion;
    private FlashPromotionSession flashSession;
    private List<FlashProductView> flashProducts;
    private List<Subject> subjects;
    private List<PrefrenceAreaView> prefrenceAreas;
    private List<ProductCategory> categories;
}
