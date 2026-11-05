package com.macro.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.macro.mall.entity.SkuStock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface SkuStockMapper extends BaseMapper<SkuStock> {
    @Update("UPDATE pms_sku_stock SET stock = stock - #{qty}, lock_stock = lock_stock + #{qty}, " +
            "sale = sale + #{qty} WHERE id = #{id} AND stock >= #{qty}")
    int lockStock(@Param("id") Long id, @Param("qty") int qty);

    @Update("UPDATE pms_sku_stock SET stock = stock + #{qty}, " +
            "lock_stock = GREATEST(lock_stock - #{qty}, 0), sale = GREATEST(sale - #{qty}, 0) WHERE id = #{id}")
    int unlockStock(@Param("id") Long id, @Param("qty") int qty);
}
