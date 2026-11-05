package com.macro.mall.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderMoneyParam {
    private Long orderId;
    private BigDecimal freightAmount;
    private BigDecimal discountAmount;
}
