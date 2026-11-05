package com.macro.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sms_coupon_history")
public class CouponHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long couponId;
    private Long memberId;
    private String memberNickname;
    private String couponCode;
    private LocalDateTime getTime;
    private Integer useStatus;
    private LocalDateTime useTime;
    private Long orderId;
    private String orderSn;
}
