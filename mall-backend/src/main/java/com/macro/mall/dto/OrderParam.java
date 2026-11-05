package com.macro.mall.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderParam {
    private List<Long> cartIds;
    private Long addressId;
    private Long couponHistoryId;
    private Integer useIntegration;
    private String note;
}
