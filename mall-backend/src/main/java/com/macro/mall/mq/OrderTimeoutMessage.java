package com.macro.mall.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTimeoutMessage implements Serializable {
    private Long orderId;
    private String orderSn;
}
