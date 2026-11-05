package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("pms_product_category")
public class ProductCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private Integer productCount;
    private String productUnit;
    private Integer navStatus;
    private Integer showStatus;
    private Integer sort;
    private String icon;
    private String keywords;
    private String description;

    @TableField(exist = false)
    private List<ProductCategory> children;
}
