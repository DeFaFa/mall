package com.macro.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.macro.mall.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ProductMapper extends BaseMapper<Product> {
    @Update("UPDATE pms_product SET stock = stock - #{qty}, sale = sale + #{qty} WHERE id = #{id} AND stock >= #{qty}")
    int lockStock(@Param("id") Long id, @Param("qty") int qty);

    @Update("UPDATE pms_product SET stock = stock + #{qty}, sale = GREATEST(sale - #{qty}, 0) WHERE id = #{id}")
    int unlockStock(@Param("id") Long id, @Param("qty") int qty);
}
