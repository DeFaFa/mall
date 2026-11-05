package com.macro.mall.dto;

import com.macro.mall.entity.Member;
import com.macro.mall.entity.MemberLevel;
import com.macro.mall.entity.MemberReceiveAddress;
import lombok.Data;

import java.util.List;

@Data
public class MemberInfo {
    private Member member;
    private MemberLevel level;
    private List<MemberReceiveAddress> addressList;
    private long collectionCount;
    private long attentionCount;
    private long historyCount;
    private long couponCount;
    private long unusedCouponCount;
}
