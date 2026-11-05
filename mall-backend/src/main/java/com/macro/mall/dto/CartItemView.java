package com.macro.mall.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemView {
    private Long id;
    private Long productId;
    private Long productCategoryId;
    private Long skuId;
    private Integer quantity;
    private String productName;
    private String subTitle;
    private String pic;
    private String skuCode;
    private String spData;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal promotionAmount;
    private Integer stock;
}
