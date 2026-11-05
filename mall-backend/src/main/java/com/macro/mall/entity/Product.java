package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pms_product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productCategoryId;
    private Long brandId;
    private String name;
    private String subTitle;
    private String description;
    private String productSn;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer lowStock;
    private String pic;
    private String albumPics;
    private String unit;
    private BigDecimal weight;
    private Integer sort;
    private Integer sale;
    private Integer publishStatus;
    private Integer newStatus;
    private Integer recommandStatus;
    private Integer verifyStatus;
    private Integer deleteStatus;
    private String keywords;
    private String detailTitle;
    private String detailDesc;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
