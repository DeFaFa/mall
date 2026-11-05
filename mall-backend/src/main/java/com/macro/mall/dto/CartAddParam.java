package com.macro.mall.dto;

import lombok.Data;

@Data
public class CartAddParam {
    private Long productId;
    private Long skuId;
    private Integer quantity;
}
