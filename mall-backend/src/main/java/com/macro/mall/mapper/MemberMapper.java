package com.macro.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.macro.mall.entity.Member;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface MemberMapper extends BaseMapper<Member> {
    @Update("UPDATE ums_member SET integration = integration - #{points} " +
            "WHERE id = #{id} AND integration >= #{points}")
    int deductIntegration(@Param("id") Long id, @Param("points") int points);
}
