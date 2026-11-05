package com.macro.mall.dto;

import lombok.Data;

@Data
public class OrderReceiverParam {
    private Long orderId;
    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverRegion;
    private String receiverDetailAddress;
}
