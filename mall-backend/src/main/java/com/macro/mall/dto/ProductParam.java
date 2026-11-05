package com.macro.mall.dto;

import com.macro.mall.entity.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductParam extends Product {
    private List<SkuStock> skuStockList;
    private List<ProductAttributeValue> productAttributeValueList;
    private List<MemberPrice> memberPriceList;
    private List<ProductLadder> productLadderList;
    private List<ProductFullReduction> productFullReductionList;
    private List<Long> subjectIds;
    private List<Long> prefrenceAreaIds;
}
