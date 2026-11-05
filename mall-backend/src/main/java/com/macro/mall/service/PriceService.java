package com.macro.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macro.mall.entity.*;
import com.macro.mall.mapper.FlashPromotionMapper;
import com.macro.mall.mapper.FlashPromotionProductRelationMapper;
import com.macro.mall.mapper.FlashPromotionSessionMapper;
import com.macro.mall.mapper.MemberLevelMapper;
import com.macro.mall.mapper.MemberPriceMapper;
import com.macro.mall.mapper.ProductLadderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 价格计算：秒杀价 > 会员价/会员折扣 > SKU 价，再叠加满足条件的阶梯价。
 * 购物车不保存价格，所有价格都在这里实时计算。
 */
@Service
@RequiredArgsConstructor
public class PriceService {
    private final MemberPriceMapper memberPriceMapper;
    private final ProductLadderMapper ladderMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final FlashPromotionMapper flashPromotionMapper;
    private final FlashPromotionSessionMapper flashSessionMapper;
    private final FlashPromotionProductRelationMapper flashRelationMapper;

    public BigDecimal unitPrice(Product product, SkuStock sku, int quantity, Long memberLevelId) {
        BigDecimal price = sku.getPrice() == null ? BigDecimal.ZERO : sku.getPrice();
        BigDecimal flashPrice = activeFlashPrice(product.getId());
        boolean flashApplied = false;
        if (flashPrice != null && flashPrice.compareTo(BigDecimal.ZERO) > 0) {
            price = flashPrice;
            flashApplied = true;
        }
        if (!flashApplied && memberLevelId != null) {
            MemberPrice memberPrice = memberPriceMapper.selectOne(new LambdaQueryWrapper<MemberPrice>()
                    .eq(MemberPrice::getProductId, product.getId())
                    .eq(MemberPrice::getMemberLevelId, memberLevelId)
                    .last("limit 1"));
            if (memberPrice != null && memberPrice.getMemberPrice() != null) {
                price = memberPrice.getMemberPrice();
            } else {
                MemberLevel level = memberLevelMapper.selectById(memberLevelId);
                if (level != null && level.getProductDiscount() != null
                        && level.getProductDiscount().compareTo(BigDecimal.TEN) < 0
                        && level.getProductDiscount().compareTo(BigDecimal.ZERO) > 0) {
                    price = price.multiply(level.getProductDiscount())
                            .divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
                }
            }
        }
        // 阶梯价：满足数量门槛时取最低可成交价
        List<ProductLadder> ladders = ladderMapper.selectList(new LambdaQueryWrapper<ProductLadder>()
                .eq(ProductLadder::getProductId, product.getId())
                .le(ProductLadder::getCount, quantity)
                .and(w -> w.eq(ProductLadder::getMemberLevelId, 0)
                        .or(memberLevelId != null, q -> q.eq(ProductLadder::getMemberLevelId, memberLevelId))));
        for (ProductLadder ladder : ladders) {
            if (ladder.getPrice() != null && ladder.getPrice().compareTo(price) < 0) {
                price = ladder.getPrice();
            }
        }
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal activeFlashPrice(Long productId) {
        FlashPromotionProductRelation relation = activeFlashRelation(productId);
        return relation == null ? null : relation.getFlashPromotionPrice();
    }

    public FlashPromotionProductRelation activeFlashRelation(Long productId) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        List<FlashPromotion> promotions = flashPromotionMapper.selectList(new LambdaQueryWrapper<FlashPromotion>()
                .eq(FlashPromotion::getStatus, 1)
                .le(FlashPromotion::getStartDate, today)
                .ge(FlashPromotion::getEndDate, today));
        if (promotions.isEmpty()) {
            return null;
        }
        List<Long> promotionIds = promotions.stream().map(FlashPromotion::getId).toList();
        List<FlashPromotionSession> sessions = flashSessionMapper.selectList(new LambdaQueryWrapper<FlashPromotionSession>()
                .eq(FlashPromotionSession::getStatus, 1)
                .le(FlashPromotionSession::getStartTime, now)
                .ge(FlashPromotionSession::getEndTime, now));
        if (sessions.isEmpty()) {
            return null;
        }
        List<Long> sessionIds = sessions.stream().map(FlashPromotionSession::getId).toList();
        return flashRelationMapper.selectOne(new LambdaQueryWrapper<FlashPromotionProductRelation>()
                .eq(FlashPromotionProductRelation::getProductId, productId)
                .in(FlashPromotionProductRelation::getFlashPromotionId, promotionIds)
                .in(FlashPromotionProductRelation::getFlashPromotionSessionId, sessionIds)
                .last("limit 1"));
    }

    public MemberLevel levelOf(Long memberLevelId) {
        return memberLevelId == null ? null : memberLevelMapper.selectById(memberLevelId);
    }

    public BigDecimal freeFreightPoint(Long memberLevelId) {
        MemberLevel level = levelOf(memberLevelId);
        return level == null || level.getFreeFreightPoint() == null ? BigDecimal.ZERO : level.getFreeFreightPoint();
    }

    public FlashPromotionSession currentSession() {
        LocalTime now = LocalTime.now();
        return flashSessionMapper.selectOne(new LambdaQueryWrapper<FlashPromotionSession>()
                .eq(FlashPromotionSession::getStatus, 1)
                .le(FlashPromotionSession::getStartTime, now)
                .ge(FlashPromotionSession::getEndTime, now)
                .last("limit 1"));
    }
}
