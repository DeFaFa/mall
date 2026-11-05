package com.macro.mall.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReturnHandleParam {
    private Long id;
    private Integer status;
    private String note;
    private BigDecimal refundAmount;
}
