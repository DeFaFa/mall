package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("sms_flash_promotion_product_relation")
public class FlashPromotionProductRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long flashPromotionId;
    private Long flashPromotionSessionId;
    private Long productId;
    private BigDecimal flashPromotionPrice;
    private Integer flashPromotionCount;
    private Integer flashPromotionLimit;
    private Integer sort;

    @TableField(exist = false)
    private String productName;
}
