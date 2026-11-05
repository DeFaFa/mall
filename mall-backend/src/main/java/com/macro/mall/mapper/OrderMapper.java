package com.macro.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.macro.mall.entity.Order;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface OrderMapper extends BaseMapper<Order> {
    /**
     * 条件更新订单状态：只有当前状态与期望状态一致时才会更新成功。
     * 超时取消、用户取消、支付回调都依赖该 CAS 语义实现幂等。
     */
    @Update("UPDATE oms_order SET status = #{toStatus}, modify_time = NOW() " +
            "WHERE id = #{id} AND status = #{fromStatus} AND delete_status = 0")
    int updateStatusIfMatch(@Param("id") Long id,
                            @Param("fromStatus") int fromStatus,
                            @Param("toStatus") int toStatus);
}
