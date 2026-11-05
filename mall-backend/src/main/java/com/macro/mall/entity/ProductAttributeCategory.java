package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pms_product_attribute_category")
public class ProductAttributeCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long productCategoryId;
    private LocalDateTime createTime;
}
