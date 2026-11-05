package com.macro.mall.dto;

import com.macro.mall.entity.Coupon;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CouponParam extends Coupon {
    private List<Long> productIds;
    private List<Long> categoryIds;
}
