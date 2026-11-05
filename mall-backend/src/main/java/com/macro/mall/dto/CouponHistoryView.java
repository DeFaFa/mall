package com.macro.mall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CouponHistoryView {
    private Long id;
    private Long couponId;
    private String name;
    private Integer type;
    private BigDecimal amount;
    private BigDecimal minPoint;
    private Integer useType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String note;
    private Integer useStatus;
    private String couponCode;
    private java.math.BigDecimal discount;
    private java.math.BigDecimal eligibleAmount;
}
