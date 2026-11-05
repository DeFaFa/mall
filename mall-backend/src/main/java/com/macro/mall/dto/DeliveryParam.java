package com.macro.mall.dto;

import lombok.Data;

import java.util.List;

@Data
public class DeliveryParam {
    private List<Long> ids;
    private String deliveryCompany;
    private String deliverySn;
}
