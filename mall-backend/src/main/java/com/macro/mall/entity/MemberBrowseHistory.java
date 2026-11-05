package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ums_member_browse_history")
public class MemberBrowseHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long memberId;
    private Long productId;
    private String productName;
    private String productPic;
    private BigDecimal productPrice;
    private LocalDateTime createTime;
}
