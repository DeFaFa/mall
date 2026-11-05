package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("oms_order_item")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderSn;
    private Long productId;
    private Long productSkuId;
    private String productName;
    private String productPic;
    private BigDecimal productPrice;
    private Integer productQuantity;
    private String productSku;
    private String productAttr;
    private BigDecimal realAmount;
    private BigDecimal promotionAmount;
    private BigDecimal couponAmount;
    private BigDecimal integrationAmount;
}
