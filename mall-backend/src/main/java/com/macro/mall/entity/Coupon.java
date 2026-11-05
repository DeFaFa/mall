package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("sms_coupon")
public class Coupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer type;
    private Integer platform;
    private Integer count;
    private BigDecimal amount;
    private BigDecimal minPoint;
    private Integer perLimit;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String note;
    private Integer useType;
    private LocalDateTime createTime;
}
