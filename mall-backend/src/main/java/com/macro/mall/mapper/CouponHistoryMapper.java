package com.macro.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.macro.mall.entity.CouponHistory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface CouponHistoryMapper extends BaseMapper<CouponHistory> {
    @Update("UPDATE sms_coupon_history SET use_status = 1, use_time = NOW(), order_id = #{orderId}, " +
            "order_sn = #{orderSn} WHERE id = #{id} AND use_status = 0")
    int markUsed(@Param("id") Long id, @Param("orderId") Long orderId, @Param("orderSn") String orderSn);

    @Update("UPDATE sms_coupon_history SET use_status = 0, use_time = NULL, order_id = 0, order_sn = '' " +
            "WHERE order_id = #{orderId} AND use_status = 1")
    int refundByOrder(@Param("orderId") Long orderId);
}
