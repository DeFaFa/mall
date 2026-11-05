package com.macro.mall.dto;

import com.macro.mall.entity.MemberReceiveAddress;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderConfirmResult {
    private List<CartItemView> items;
    private List<MemberReceiveAddress> addressList;
    private MemberReceiveAddress address;
    private List<CouponHistoryView> couponHistoryList;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private BigDecimal freightAmount;
    private BigDecimal promotionAmount;
    private BigDecimal couponAmount;
    private BigDecimal integrationAmount;
    private BigDecimal discountAmount;
    private Integer useIntegration;
    private Integer memberIntegration;
}
