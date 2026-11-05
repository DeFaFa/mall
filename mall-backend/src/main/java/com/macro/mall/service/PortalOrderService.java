package com.macro.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macro.mall.common.BizException;
import com.macro.mall.dto.*;
import com.macro.mall.entity.*;
import com.macro.mall.mapper.*;
import com.macro.mall.mq.OrderTimeoutSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortalOrderService {
    private static final BigDecimal FREIGHT = new BigDecimal("12.00");

    private final CartService cartService;
    private final MemberMapper memberMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final MemberReceiveAddressMapper addressMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final SkuStockMapper skuStockMapper;
    private final ProductFullReductionMapper fullReductionMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderOperateHistoryMapper historyMapper;
    private final OrderReturnApplyMapper returnApplyMapper;
    private final OrderReturnReasonMapper returnReasonMapper;
    private final CouponMapper couponMapper;
    private final CouponHistoryMapper couponHistoryMapper;
    private final CouponService couponService;
    private final PriceService priceService;
    private final OrderSnGenerator orderSnGenerator;
    private final OrderTimeoutSender timeoutSender;

    public OrderConfirmResult confirm(Long memberId, OrderParam param) {
        return calculate(memberId, param);
    }

    private OrderConfirmResult calculate(Long memberId, OrderParam param) {
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new BizException("会员不存在");
        }
        List<CartItemView> all = cartService.list(memberId);
        List<CartItemView> items;
        if (param.getCartIds() == null || param.getCartIds().isEmpty()) {
            items = all;
        } else {
            items = all.stream().filter(i -> param.getCartIds().contains(i.getId())).collect(Collectors.toList());
            if (items.size() != param.getCartIds().size()) {
                throw new BizException("购物车商品已失效，请刷新后重试");
            }
        }
        if (items.isEmpty()) {
            throw new BizException("请先选择要结算的商品");
        }

        List<MemberReceiveAddress> addresses = addressMapper.selectList(new LambdaQueryWrapper<MemberReceiveAddress>()
                .eq(MemberReceiveAddress::getMemberId, memberId)
                .orderByDesc(MemberReceiveAddress::getDefaultStatus)
                .orderByDesc(MemberReceiveAddress::getId));
        MemberReceiveAddress address = null;
        if (param.getAddressId() != null) {
            address = addresses.stream().filter(a -> a.getId().equals(param.getAddressId())).findFirst()
                    .orElseThrow(() -> new BizException("收货地址不存在"));
        } else if (!addresses.isEmpty()) {
            address = addresses.get(0);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal goodsAmount = BigDecimal.ZERO;
        for (CartItemView item : items) {
            BigDecimal original = item.getOriginalPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            BigDecimal real = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(original);
            goodsAmount = goodsAmount.add(real);
        }
        BigDecimal fullReduction = calcFullReduction(items);
        BigDecimal couponAmount = BigDecimal.ZERO;
        if (param.getCouponHistoryId() != null) {
            CouponHistoryView coupon = couponService.calculate(memberId, param.getCouponHistoryId(), items);
            couponAmount = coupon.getDiscount();
        }
        BigDecimal afterCoupon = goodsAmount.subtract(fullReduction).subtract(couponAmount);
        if (afterCoupon.compareTo(BigDecimal.ZERO) < 0) {
            afterCoupon = BigDecimal.ZERO;
        }
        int maxIntegrationPoints = afterCoupon.multiply(BigDecimal.TEN)
                .setScale(0, RoundingMode.FLOOR).intValue();
        int requestedPoints = param.getUseIntegration() == null ? 0 : Math.max(0, param.getUseIntegration());
        int usePoints = Math.min(requestedPoints, Math.min(member.getIntegration() == null ? 0 : member.getIntegration(),
                maxIntegrationPoints));
        BigDecimal integrationAmount = BigDecimal.valueOf(usePoints)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal freePoint = priceService.freeFreightPoint(member.getMemberLevelId());
        BigDecimal freight = goodsAmount.compareTo(freePoint) >= 0 ? BigDecimal.ZERO : FREIGHT;
        BigDecimal payAmount = goodsAmount.subtract(fullReduction).subtract(couponAmount)
                .subtract(integrationAmount).add(freight);
        if (payAmount.compareTo(new BigDecimal("0.01")) < 0) {
            payAmount = new BigDecimal("0.01");
        }

        OrderConfirmResult result = new OrderConfirmResult();
        result.setItems(items);
        result.setAddressList(addresses);
        result.setAddress(address);
        result.setCouponHistoryList(couponService.usableForCart(memberId, items));
        result.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        result.setPayAmount(payAmount.setScale(2, RoundingMode.HALF_UP));
        result.setFreightAmount(freight);
        result.setPromotionAmount(fullReduction.setScale(2, RoundingMode.HALF_UP));
        result.setCouponAmount(couponAmount.setScale(2, RoundingMode.HALF_UP));
        result.setIntegrationAmount(integrationAmount);
        result.setDiscountAmount(totalAmount.subtract(goodsAmount).setScale(2, RoundingMode.HALF_UP));
        result.setUseIntegration(usePoints);
        result.setMemberIntegration(member.getIntegration() == null ? 0 : member.getIntegration());
        return result;
    }

    private BigDecimal calcFullReduction(List<CartItemView> items) {
        Map<Long, BigDecimal> subtotalMap = new HashMap<>();
        for (CartItemView item : items) {
            subtotalMap.merge(item.getProductId(),
                    item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())), BigDecimal::add);
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal> entry : subtotalMap.entrySet()) {
            List<ProductFullReduction> rules = fullReductionMapper.selectList(
                    new LambdaQueryWrapper<ProductFullReduction>()
                            .eq(ProductFullReduction::getProductId, entry.getKey()));
            BigDecimal best = BigDecimal.ZERO;
            for (ProductFullReduction rule : rules) {
                if (entry.getValue().compareTo(rule.getFullPrice()) >= 0
                        && rule.getReducePrice().compareTo(best) > 0) {
                    best = rule.getReducePrice();
                }
            }
            total = total.add(best);
        }
        return total;
    }

    @Transactional
    public Long generate(Long memberId, OrderParam param) {
        Member member = memberMapper.selectById(memberId);
        OrderConfirmResult calc = calculate(memberId, param);
        if (calc.getAddress() == null) {
            throw new BizException("请先添加收货地址");
        }
        if (calc.getItems().isEmpty()) {
            throw new BizException("请选择商品");
        }
        MemberReceiveAddress address = calc.getAddress();

        // 锁定库存：可用库存转锁定库存，条件更新失败即视为库存不足
        for (CartItemView item : calc.getItems()) {
            SkuStock sku = skuStockMapper.selectById(item.getSkuId());
            if (sku == null || sku.getStock() < item.getQuantity()) {
                throw new BizException("商品 " + item.getProductName() + " 库存不足");
            }
            if (skuStockMapper.lockStock(sku.getId(), item.getQuantity()) == 0) {
                throw new BizException("商品 " + item.getProductName() + " 库存不足");
            }
            if (productMapper.lockStock(item.getProductId(), item.getQuantity()) == 0) {
                throw new BizException("商品 " + item.getProductName() + " 库存不足");
            }
        }

        String orderSn = orderSnGenerator.next();
        Order order = new Order();
        order.setOrderSn(orderSn);
        order.setMemberId(memberId);
        order.setMemberUsername(member == null ? "" : member.getUsername());
        order.setCouponId(0L);
        if (param.getCouponHistoryId() != null) {
            CouponHistory history = couponHistoryMapper.selectById(param.getCouponHistoryId());
            order.setCouponId(history == null ? 0L : history.getCouponId());
        }
        order.setOrderType(0);
        order.setTotalAmount(calc.getTotalAmount());
        order.setPayAmount(calc.getPayAmount());
        order.setFreightAmount(calc.getFreightAmount());
        order.setPromotionAmount(calc.getPromotionAmount());
        order.setCouponAmount(calc.getCouponAmount());
        order.setIntegrationAmount(calc.getIntegrationAmount());
        order.setDiscountAmount(calc.getTotalAmount().subtract(calc.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)).max(BigDecimal.ZERO));
        order.setPayType(0);
        order.setSourceType(1);
        order.setStatus(0);
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverDetailAddress(address.getDetailAddress());
        order.setNote(param.getNote());
        order.setConfirmStatus(0);
        order.setDeleteStatus(0);
        order.setIntegration(calc.getUseIntegration());
        order.setGrowth(0);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);

        BigDecimal goodsReal = calc.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal couponAllocateBase = goodsReal.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : goodsReal;
        for (CartItemView item : calc.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setOrderSn(orderSn);
            orderItem.setProductId(item.getProductId());
            orderItem.setProductSkuId(item.getSkuId());
            orderItem.setProductName(item.getProductName());
            orderItem.setProductPic(item.getPic());
            orderItem.setProductPrice(item.getPrice());
            orderItem.setProductQuantity(item.getQuantity());
            orderItem.setProductSku(item.getSpData());
            orderItem.setProductAttr(item.getSpData());
            BigDecimal lineAmount = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            orderItem.setRealAmount(lineAmount);
            orderItem.setPromotionAmount(item.getOriginalPrice().subtract(item.getPrice())
                    .multiply(BigDecimal.valueOf(item.getQuantity())));
            BigDecimal ratio = lineAmount.divide(couponAllocateBase, 6, RoundingMode.HALF_UP);
            orderItem.setCouponAmount(calc.getCouponAmount().multiply(ratio).setScale(2, RoundingMode.HALF_UP));
            orderItem.setIntegrationAmount(calc.getIntegrationAmount().multiply(ratio).setScale(2, RoundingMode.HALF_UP));
            orderItemMapper.insert(orderItem);
        }

        if (param.getCouponHistoryId() != null) {
            int marked = couponHistoryMapper.markUsed(param.getCouponHistoryId(), order.getId(), orderSn);
            if (marked == 0) {
                throw new BizException("优惠券已被使用，请重新选择");
            }
        }
        if (calc.getUseIntegration() > 0) {
            int deducted = memberMapper.deductIntegration(memberId, calc.getUseIntegration());
            if (deducted == 0) {
                throw new BizException("积分不足");
            }
        }

        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getMemberId, memberId)
                .in(CartItem::getId, calc.getItems().stream().map(CartItemView::getId).toList()));

        saveHistory(order.getId(), member == null ? "会员" : member.getUsername(), 0, "会员下单");

        Long orderId = order.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 事务提交后再发延迟消息，避免回滚后残留无效消息
                timeoutSender.send(orderId, orderSn);
            }
        });
        return orderId;
    }

    public OrderPayResult pay(Long memberId, Long orderId) {
        Order order = ownedOrder(memberId, orderId);
        if (order.getStatus() != 0) {
            throw new BizException("订单当前状态不可支付");
        }
        OrderPayResult result = new OrderPayResult();
        result.setOrderId(order.getId());
        result.setOrderSn(order.getOrderSn());
        result.setPayAmount(order.getPayAmount());
        result.setMock(true);
        result.setPayUrl("/portal/alipay/mock-cashier?orderId=" + order.getId()
                + "&orderSn=" + order.getOrderSn() + "&payAmount=" + order.getPayAmount());
        return result;
    }

    /**
     * 支付成功回调（演示环境由模拟收银台触发）。
     * 使用条件更新保证重复回调只生效一次。
     */
    @Transactional
    public void paySuccess(Long orderId, String tradeNo) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (order.getStatus() == 1) {
            log.info("订单已支付，忽略重复回调 orderId={}", orderId);
            return;
        }
        if (order.getStatus() != 0) {
            throw new BizException("订单状态异常，无法支付");
        }
        int updated = orderMapper.updateStatusIfMatch(orderId, 0, 1);
        if (updated == 0) {
            log.info("支付回调幂等忽略 orderId={}", orderId);
            return;
        }
        Order update = new Order();
        update.setId(orderId);
        update.setPayType(1);
        update.setPaymentTime(LocalDateTime.now());
        int points = order.getPayAmount().setScale(0, RoundingMode.HALF_UP).intValue();
        update.setGrowth(points);
        orderMapper.updateById(update);

        if (points > 0) {
            memberMapper.update(null, new LambdaUpdateWrapper<Member>()
                    .eq(Member::getId, order.getMemberId())
                    .setSql("integration = integration + " + points + ", growth = growth + " + points));
        }
        saveHistory(orderId, "system", 1, "支付成功" + (tradeNo == null ? "" : "，交易号：" + tradeNo));
    }

    @Transactional
    public boolean cancel(Long memberId, Long orderId) {
        Order order = ownedOrder(memberId, orderId);
        if (order.getStatus() != 0) {
            throw new BizException("仅待付款订单可以取消");
        }
        return closeUnpaid(order, "member:" + memberId, "用户取消订单");
    }

    /**
     * 由 RabbitMQ 死信队列触发。条件更新成功才回滚资源，保证重复消息的幂等。
     */
    @Transactional
    public boolean timeoutCancel(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.warn("超时取消时订单不存在 orderId={}", orderId);
            return false;
        }
        return closeUnpaid(order, "system", "超时未支付，订单自动关闭");
    }

    @Transactional
    public boolean closeUnpaid(Order order, String operator, String note) {
        int updated = orderMapper.updateStatusIfMatch(order.getId(), 0, 4);
        if (updated == 0) {
            log.info("订单已支付或已关闭，取消操作忽略 orderId={}", order.getId());
            return false;
        }
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : items) {
            skuStockMapper.unlockStock(item.getProductSkuId(), item.getProductQuantity());
            productMapper.unlockStock(item.getProductId(), item.getProductQuantity());
        }
        couponHistoryMapper.refundByOrder(order.getId());
        if (order.getIntegration() != null && order.getIntegration() > 0) {
            memberMapper.update(null, new LambdaUpdateWrapper<Member>()
                    .eq(Member::getId, order.getMemberId())
                    .setSql("integration = integration + " + order.getIntegration()));
        }
        saveHistory(order.getId(), operator, 4, note);
        return true;
    }

    public Page<Order> page(Long memberId, Integer status, int pageNum, int pageSize) {
        return orderMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<Order>()
                .eq(Order::getMemberId, memberId)
                .eq(Order::getDeleteStatus, 0)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getId));
    }

    public OrderDetail detail(Long memberId, Long orderId) {
        Order order = ownedOrder(memberId, orderId);
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setOrderItemList(orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId)));
        detail.setHistoryList(historyMapper.selectList(new LambdaQueryWrapper<OrderOperateHistory>()
                .eq(OrderOperateHistory::getOrderId, orderId)
                .orderByAsc(OrderOperateHistory::getCreateTime)));
        detail.setReturnApply(returnApplyMapper.selectOne(new LambdaQueryWrapper<OrderReturnApply>()
                .eq(OrderReturnApply::getOrderId, orderId)
                .orderByDesc(OrderReturnApply::getId).last("limit 1")));
        return detail;
    }

    public OrderDetail adminDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setOrderItemList(orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId)));
        detail.setHistoryList(historyMapper.selectList(new LambdaQueryWrapper<OrderOperateHistory>()
                .eq(OrderOperateHistory::getOrderId, orderId)
                .orderByAsc(OrderOperateHistory::getCreateTime)));
        detail.setReturnApply(returnApplyMapper.selectOne(new LambdaQueryWrapper<OrderReturnApply>()
                .eq(OrderReturnApply::getOrderId, orderId)
                .orderByDesc(OrderReturnApply::getId).last("limit 1")));
        return detail;
    }

    public void confirmReceive(Long memberId, Long orderId) {
        Order order = ownedOrder(memberId, orderId);
        if (order.getStatus() != 2) {
            throw new BizException("仅已发货订单可以确认收货");
        }
        int updated = orderMapper.updateStatusIfMatch(orderId, 2, 3);
        if (updated == 0) {
            throw new BizException("订单状态已变化，请刷新");
        }
        Order update = new Order();
        update.setId(orderId);
        update.setReceiveTime(LocalDateTime.now());
        orderMapper.updateById(update);
        saveHistory(orderId, "member:" + memberId, 3, "确认收货");
    }

    public void delete(Long memberId, Long orderId) {
        Order order = ownedOrder(memberId, orderId);
        if (order.getStatus() != 3 && order.getStatus() != 4) {
            throw new BizException("只有已完成或已关闭的订单可以删除");
        }
        Order update = new Order();
        update.setId(orderId);
        update.setDeleteStatus(1);
        orderMapper.updateById(update);
    }

    public OrderStatusCount statusCount(Long memberId) {
        OrderStatusCount count = new OrderStatusCount();
        count.setUnpaid(count(memberId, 0));
        count.setUnshipped(count(memberId, 1));
        count.setShipped(count(memberId, 2));
        count.setCompleted(count(memberId, 3));
        return count;
    }

    private long count(Long memberId, int status) {
        return orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getMemberId, memberId)
                .eq(Order::getDeleteStatus, 0)
                .eq(Order::getStatus, status));
    }

    @Transactional
    public Long applyReturn(Long memberId, OrderReturnApply apply) {
        Order order = ownedOrder(memberId, apply.getOrderId());
        if (order.getStatus() != 2 && order.getStatus() != 3) {
            throw new BizException("当前订单状态不支持退货");
        }
        long exists = returnApplyMapper.selectCount(new LambdaQueryWrapper<OrderReturnApply>()
                .eq(OrderReturnApply::getOrderId, order.getId())
                .in(OrderReturnApply::getStatus, 0, 1));
        if (exists > 0) {
            throw new BizException("该订单已有进行中的退货申请");
        }
        OrderReturnReason reason = apply.getReturnReasonId() == null ? null
                : returnReasonMapper.selectById(apply.getReturnReasonId());
        Member member = memberMapper.selectById(memberId);
        apply.setId(null);
        apply.setMemberId(memberId);
        apply.setOrderSn(order.getOrderSn());
        if (apply.getReturnName() == null || apply.getReturnName().isBlank()) {
            apply.setReturnName(member == null || member.getNickname() == null
                    ? order.getReceiverName() : member.getNickname());
        }
        if (apply.getReturnPhone() == null || apply.getReturnPhone().isBlank()) {
            apply.setReturnPhone(member == null ? order.getReceiverPhone() : member.getPhone());
        }
        apply.setStatus(0);
        apply.setReturnReasonName(reason == null ? "" : reason.getName());
        apply.setRefundAmount(order.getPayAmount());
        apply.setCreateTime(LocalDateTime.now());
        returnApplyMapper.insert(apply);
        return apply.getId();
    }

    public List<OrderReturnApply> returns(Long memberId) {
        return returnApplyMapper.selectList(new LambdaQueryWrapper<OrderReturnApply>()
                .eq(OrderReturnApply::getMemberId, memberId).orderByDesc(OrderReturnApply::getId));
    }

    public List<OrderReturnReason> returnReasons() {
        return returnReasonMapper.selectList(new LambdaQueryWrapper<OrderReturnReason>()
                .eq(OrderReturnReason::getStatus, 1).orderByAsc(OrderReturnReason::getSort));
    }

    private Order ownedOrder(Long memberId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getMemberId().equals(memberId)) {
            throw new BizException("订单不存在");
        }
        return order;
    }

    private void saveHistory(Long orderId, String operator, int status, String note) {
        OrderOperateHistory history = new OrderOperateHistory();
        history.setOrderId(orderId);
        history.setOperateMan(operator);
        history.setCreateTime(LocalDateTime.now());
        history.setOrderStatus(status);
        history.setNote(note);
        historyMapper.insert(history);
    }
}
