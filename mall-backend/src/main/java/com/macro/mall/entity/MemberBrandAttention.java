package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ums_member_brand_attention")
public class MemberBrandAttention {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private Long brandId;
    private String brandName;
    private LocalDateTime createTime;
}
