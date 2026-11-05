package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("pms_product_attribute")
public class ProductAttribute {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productAttributeCategoryId;
    private String name;
    private Integer type;
    private Integer inputType;
    private String inputList;
    private Integer sort;
    private Integer filterType;
    private Integer searchType;
    private Integer relatedStatus;
    private Integer handAddStatus;
}
