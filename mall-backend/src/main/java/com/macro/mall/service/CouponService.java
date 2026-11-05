package com.macro.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.macro.mall.common.BizException;
import com.macro.mall.dto.CartItemView;
import com.macro.mall.dto.CouponHistoryView;
import com.macro.mall.entity.Coupon;
import com.macro.mall.entity.CouponHistory;
import com.macro.mall.entity.Member;
import com.macro.mall.mapper.CouponHistoryMapper;
import com.macro.mall.mapper.CouponMapper;
import com.macro.mall.mapper.LinkMapper;
import com.macro.mall.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponMapper couponMapper;
    private final CouponHistoryMapper couponHistoryMapper;
    private final LinkMapper linkMapper;
    private final MemberMapper memberMapper;

    public List<CouponHistoryView> myCoupons(Long memberId, Integer useStatus) {
        List<CouponHistory> histories = couponHistoryMapper.selectList(new LambdaQueryWrapper<CouponHistory>()
                .eq(CouponHistory::getMemberId, memberId)
                .eq(useStatus != null, CouponHistory::getUseStatus, useStatus)
                .orderByDesc(CouponHistory::getId));
        List<CouponHistoryView> views = new ArrayList<>();
        for (CouponHistory history : histories) {
            Coupon coupon = couponMapper.selectById(history.getCouponId());
            if (coupon != null) {
                views.add(toView(history, coupon));
            }
        }
        return views;
    }

    public List<CouponHistoryView> usableForCart(Long memberId, List<CartItemView> items) {
        List<CouponHistoryView> result = new ArrayList<>();
        for (CouponHistoryView view : myCoupons(memberId, 0)) {
            if (!inTime(view)) {
                continue;
            }
            Coupon coupon = couponMapper.selectById(view.getCouponId());
            try {
                BigDecimal discount = discount(coupon, items);
                if (discount.compareTo(BigDecimal.ZERO) > 0) {
                    view.setDiscount(discount);
                    view.setEligibleAmount(eligibleAmount(coupon, items));
                    result.add(view);
                }
            } catch (BizException ignored) {
                // 不满足门槛/范围，直接跳过
            }
        }
        return result;
    }

    public CouponHistoryView calculate(Long memberId, Long historyId, List<CartItemView> items) {
        CouponHistory history = couponHistoryMapper.selectById(historyId);
        if (history == null || !history.getMemberId().equals(memberId)) {
            throw new BizException("优惠券不存在");
        }
        if (history.getUseStatus() == 1) {
            throw new BizException("优惠券已使用");
        }
        Coupon coupon = couponMapper.selectById(history.getCouponId());
        if (coupon == null || !inTime(coupon)) {
            throw new BizException("优惠券已过期");
        }
        CouponHistoryView view = toView(history, coupon);
        BigDecimal discount = discount(coupon, items);
        view.setDiscount(discount);
        view.setEligibleAmount(eligibleAmount(coupon, items));
        return view;
    }

    public BigDecimal discount(Coupon coupon, List<CartItemView> items) {
        BigDecimal eligible = eligibleAmount(coupon, items);
        if (eligible.compareTo(coupon.getMinPoint() == null ? BigDecimal.ZERO : coupon.getMinPoint()) < 0) {
            throw new BizException("未达到优惠券使用门槛");
        }
        BigDecimal discount;
        if (coupon.getType() == 1) {
            BigDecimal rate = coupon.getAmount() == null ? BigDecimal.TEN : coupon.getAmount();
            discount = eligible.multiply(BigDecimal.TEN.subtract(rate))
                    .divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP);
        } else {
            discount = coupon.getAmount() == null ? BigDecimal.ZERO : coupon.getAmount();
        }
        if (discount.compareTo(eligible) > 0) {
            discount = eligible;
        }
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal eligibleAmount(Coupon coupon, List<CartItemView> items) {
        List<CartItemView> eligible = eligibleItems(coupon, items);
        return eligible.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<CartItemView> eligibleItems(Coupon coupon, List<CartItemView> items) {
        int useType = coupon.getUseType() == null ? 0 : coupon.getUseType();
        if (useType == 0) {
            return items;
        }
        if (useType == 1) {
            Set<Long> categoryIds = Set.copyOf(linkMapper.selectCategoryIdsByCoupon(coupon.getId()));
            return items.stream().filter(i -> categoryIds.contains(i.getProductCategoryId())).toList();
        }
        if (useType == 2) {
            Set<Long> productIds = Set.copyOf(linkMapper.selectProductIdsByCoupon(coupon.getId()));
            return items.stream().filter(i -> productIds.contains(i.getProductId())).toList();
        }
        return List.of();
    }

    @Transactional
    public void receive(Long memberId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || !inTime(coupon)) {
            throw new BizException("优惠券不存在或已过期");
        }
        long received = couponHistoryMapper.selectCount(new LambdaQueryWrapper<CouponHistory>()
                .eq(CouponHistory::getCouponId, couponId));
        if (received >= (coupon.getCount() == null ? 0 : coupon.getCount())) {
            throw new BizException("优惠券已领完");
        }
        long mine = couponHistoryMapper.selectCount(new LambdaQueryWrapper<CouponHistory>()
                .eq(CouponHistory::getCouponId, couponId).eq(CouponHistory::getMemberId, memberId));
        int perLimit = coupon.getPerLimit() == null ? 1 : coupon.getPerLimit();
        if (mine >= perLimit) {
            throw new BizException("已达到每人限领数量");
        }
        Member member = memberMapper.selectById(memberId);
        CouponHistory history = new CouponHistory();
        history.setCouponId(couponId);
        history.setMemberId(memberId);
        history.setMemberNickname(member == null ? "" : member.getNickname());
        history.setCouponCode("C" + couponId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        history.setGetTime(LocalDateTime.now());
        history.setUseStatus(0);
        couponHistoryMapper.insert(history);
    }

    public List<Coupon> availableList() {
        LocalDateTime now = LocalDateTime.now();
        return couponMapper.selectList(new LambdaQueryWrapper<Coupon>()
                .le(Coupon::getStartTime, now)
                .ge(Coupon::getEndTime, now)
                .orderByAsc(Coupon::getId));
    }

    public List<Coupon> couponsForProduct(Long productId, Long categoryId) {
        List<Coupon> coupons = availableList();
        return coupons.stream().filter(c -> {
            int useType = c.getUseType() == null ? 0 : c.getUseType();
            if (useType == 0) {
                return true;
            }
            if (useType == 1) {
                return linkMapper.selectCategoryIdsByCoupon(c.getId()).contains(categoryId);
            }
            return linkMapper.selectProductIdsByCoupon(c.getId()).contains(productId);
        }).toList();
    }

    private boolean inTime(Coupon coupon) {
        LocalDateTime now = LocalDateTime.now();
        return coupon.getStartTime() != null && coupon.getEndTime() != null
                && !now.isBefore(coupon.getStartTime()) && !now.isAfter(coupon.getEndTime());
    }

    private boolean inTime(CouponHistoryView coupon) {
        LocalDateTime now = LocalDateTime.now();
        return coupon.getStartTime() != null && coupon.getEndTime() != null
                && !now.isBefore(coupon.getStartTime()) && !now.isAfter(coupon.getEndTime());
    }

    private CouponHistoryView toView(CouponHistory history, Coupon coupon) {
        CouponHistoryView view = new CouponHistoryView();
        view.setId(history.getId());
        view.setCouponId(coupon.getId());
        view.setName(coupon.getName());
        view.setType(coupon.getType());
        view.setAmount(coupon.getAmount());
        view.setMinPoint(coupon.getMinPoint());
        view.setUseType(coupon.getUseType());
        view.setStartTime(coupon.getStartTime());
        view.setEndTime(coupon.getEndTime());
        view.setNote(coupon.getNote());
        view.setUseStatus(history.getUseStatus());
        view.setCouponCode(history.getCouponCode());
        return view;
    }

    public List<CouponHistory> historyOfCoupon(Long couponId) {
        return couponHistoryMapper.selectList(new LambdaQueryWrapper<CouponHistory>()
                .eq(CouponHistory::getCouponId, couponId).orderByDesc(CouponHistory::getId));
    }

    public Set<Long> receivedCouponIds(Long memberId) {
        return couponHistoryMapper.selectList(new LambdaQueryWrapper<CouponHistory>()
                        .eq(CouponHistory::getMemberId, memberId))
                .stream().map(CouponHistory::getCouponId).collect(Collectors.toSet());
    }
}
