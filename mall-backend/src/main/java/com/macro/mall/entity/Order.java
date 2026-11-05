package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("oms_order")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderSn;
    private Long memberId;
    private String memberUsername;
    private Long couponId;
    private Integer orderType;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private BigDecimal freightAmount;
    private BigDecimal promotionAmount;
    private BigDecimal couponAmount;
    private BigDecimal integrationAmount;
    private BigDecimal discountAmount;
    private Integer payType;
    private Integer sourceType;
    private Integer status;
    private String deliveryCompany;
    private String deliverySn;
    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverRegion;
    private String receiverDetailAddress;
    private String note;
    private Integer confirmStatus;
    private Integer deleteStatus;
    private Integer integration;
    private Integer growth;
    private LocalDateTime createTime;
    private LocalDateTime paymentTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiveTime;
    private LocalDateTime commentTime;
    private LocalDateTime modifyTime;
}
