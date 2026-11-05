package com.macro.mall.dto;

import com.macro.mall.entity.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDetail {
    private Product product;
    private String brandName;
    private String categoryName;
    private List<SkuStock> skuStockList;
    private List<ProductAttribute> paramList;
    private List<ProductAttributeValue> attributeValueList;
    private List<MemberPrice> memberPriceList;
    private List<ProductLadder> ladderList;
    private List<ProductFullReduction> fullReductionList;
    private List<Long> subjectIds;
    private List<Long> prefrenceAreaIds;
    private BigDecimal currentPrice;
    private BigDecimal flashPrice;
}
