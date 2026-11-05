package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("pms_sku_stock")
public class SkuStock {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String skuCode;
    private BigDecimal price;
    private Integer stock;
    private Integer lockStock;
    private Integer lowStock;
    private String pic;
    private Integer sale;
    private String spData;

    @TableField(exist = false)
    private String productName;
}
