package com.macro.mall.dto;

import com.macro.mall.entity.Product;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FlashProductView {
    private Product product;
    private BigDecimal flashPrice;
    private Integer flashCount;
    private Integer flashLimit;
    private Long sessionId;
}
