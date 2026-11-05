package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sms_home_recommend_product")
public class HomeRecommendProduct {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String productName;
    private Integer recommendStatus;
    private Integer sort;
}
