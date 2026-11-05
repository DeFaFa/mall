package com.macro.mall.dto;

import lombok.Data;

@Data
public class OrderStatusCount {
    private long unpaid;
    private long unshipped;
    private long shipped;
    private long completed;
}
