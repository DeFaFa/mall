package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("oms_order_return_apply")
public class OrderReturnApply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderSn;
    private Long memberId;
    private String returnName;
    private String returnPhone;
    private Integer status;
    private String productImg;
    private Long returnReasonId;
    private String returnReasonName;
    private String description;
    private String handleMan;
    private LocalDateTime handleTime;
    private String handleNote;
    private BigDecimal refundAmount;
    private LocalDateTime createTime;
}
