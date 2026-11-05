package com.macro.mall.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderPayResult {
    private Long orderId;
    private String orderSn;
    private BigDecimal payAmount;
    private String payUrl;
    private boolean mock;
}
