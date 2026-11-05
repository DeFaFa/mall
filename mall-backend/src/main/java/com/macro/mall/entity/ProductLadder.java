package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("pms_product_ladder")
public class ProductLadder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long memberLevelId;
    private Integer count;
    private BigDecimal price;
}
