package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("pms_member_price")
public class MemberPrice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long memberLevelId;
    private BigDecimal memberPrice;
}
