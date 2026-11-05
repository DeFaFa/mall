package com.macro.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.macro.mall.common.BizException;
import com.macro.mall.dto.OrderDetail;
import com.macro.mall.entity.*;
import com.macro.mall.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderOperateHistoryMapper historyMapper;
    private final SkuStockMapper skuStockMapper;
    private final ProductMapper productMapper;
    private final OrderReturnApplyMapper returnApplyMapper;
    private final OrderReturnReasonMapper returnReasonMapper;
    private final OrderSettingMapper orderSettingMapper;
    private final PortalOrderService portalOrderService;

    public Page<Order> page(int pageNum, int pageSize, String orderSn, Integer status, Integer sourceType,
                            Integer orderType, String receiverKeyword, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .like(orderSn != null && !orderSn.isBlank(), Order::getOrderSn, orderSn)
                .eq(status != null, Order::getStatus, status)
                .eq(sourceType != null, Order::getSourceType, sourceType)
                .eq(orderType != null, Order::getOrderType, orderType)
                .and(receiverKeyword != null && !receiverKeyword.isBlank(),
                        w -> w.like(Order::getReceiverName, receiverKeyword)
                                .or().like(Order::getReceiverPhone, receiverKeyword))
                .ge(startDate != null, Order::getCreateTime,
                        startDate == null ? null : startDate.atStartOfDay())
                .le(endDate != null, Order::getCreateTime,
                        endDate == null ? null : endDate.atTime(LocalTime.MAX))
                .orderByDesc(Order::getId);
        return orderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public OrderDetail detail(Long orderId) {
        return portalOrderService.adminDetail(orderId);
    }

    @Transactional
    public void delivery(List<Long> ids, String company, String deliverySn) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException("请选择要发货的订单");
        }
        if (company == null || company.isBlank() || deliverySn == null || deliverySn.isBlank()) {
            throw new BizException("请填写物流公司和物流单号");
        }
        for (Long id : ids) {
            Order order = orderMapper.selectById(id);
            if (order == null || order.getStatus() != 1) {
                continue;
            }
            if (orderMapper.updateStatusIfMatch(id, 1, 2) == 0) {
                continue;
            }
            Order update = new Order();
            update.setId(id);
            update.setDeliveryCompany(company);
            update.setDeliverySn(deliverySn);
            update.setDeliveryTime(LocalDateTime.now());
            orderMapper.updateById(update);
            saveHistory(id, "admin", 2, "批量发货：" + company + " " + deliverySn);
        }
    }

    @Transactional
    public void close(List<Long> ids, String operator) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException("请选择要关闭的订单");
        }
        for (Long id : ids) {
            Order order = orderMapper.selectById(id);
            if (order == null) {
                continue;
            }
            if (order.getStatus() == 0) {
                portalOrderService.closeUnpaid(order, operator, "管理员关闭未付款订单");
            } else if (order.getStatus() == 1) {
                if (orderMapper.updateStatusIfMatch(id, 1, 4) == 0) {
                    continue;
                }
                restoreStock(id);
                saveHistory(id, operator, 4, "管理员关闭订单");
            }
        }
    }

    public void logicalDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException("请选择订单");
        }
        for (Long id : ids) {
            Order update = new Order();
            update.setId(id);
            update.setDeleteStatus(1);
            orderMapper.updateById(update);
        }
    }

    public void updateReceiver(Long orderId, Order param, String operator) {
        Order update = new Order();
        update.setId(orderId);
        update.setReceiverName(param.getReceiverName());
        update.setReceiverPhone(param.getReceiverPhone());
        update.setReceiverProvince(param.getReceiverProvince());
        update.setReceiverCity(param.getReceiverCity());
        update.setReceiverRegion(param.getReceiverRegion());
        update.setReceiverDetailAddress(param.getReceiverDetailAddress());
        orderMapper.updateById(update);
        saveHistory(orderId, operator, null, "修改收货人信息");
    }

    public void updateMoney(Long orderId, BigDecimal freightAmount, BigDecimal discountAmount, String operator) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        BigDecimal freight = freightAmount == null ? BigDecimal.ZERO : freightAmount;
        BigDecimal discount = discountAmount == null ? BigDecimal.ZERO : discountAmount;
        BigDecimal payAmount = order.getTotalAmount().subtract(order.getPromotionAmount())
                .subtract(order.getCouponAmount()).subtract(order.getIntegrationAmount())
                .add(freight).subtract(discount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }
        Order update = new Order();
        update.setId(orderId);
        update.setFreightAmount(freight);
        update.setDiscountAmount(discount);
        update.setPayAmount(payAmount);
        orderMapper.updateById(update);
        saveHistory(orderId, operator, null, "修改订单费用");
    }

    public void updateNote(Long orderId, String note, String operator) {
        Order update = new Order();
        update.setId(orderId);
        update.setNote(note);
        orderMapper.updateById(update);
        saveHistory(orderId, operator, null, "修改订单备注：" + note);
    }

    public Page<OrderReturnApply> returnApplies(int pageNum, int pageSize, Integer status, String keyword) {
        return returnApplyMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<OrderReturnApply>()
                .eq(status != null, OrderReturnApply::getStatus, status)
                .and(keyword != null && !keyword.isBlank(),
                        w -> w.like(OrderReturnApply::getOrderSn, keyword)
                                .or().like(OrderReturnApply::getReturnName, keyword))
                .orderByDesc(OrderReturnApply::getId));
    }

    public OrderReturnApply returnApplyDetail(Long id) {
        OrderReturnApply apply = returnApplyMapper.selectById(id);
        if (apply == null) {
            throw new BizException("退货申请不存在");
        }
        return apply;
    }

    @Transactional
    public void handleReturn(Long id, Integer status, String note, BigDecimal refundAmount, String operator) {
        if (status == null || status < 1 || status > 2) {
            throw new BizException("审核状态不正确");
        }
        OrderReturnApply apply = returnApplyDetail(id);
        if (apply.getStatus() != 0) {
            throw new BizException("该申请已处理");
        }
        apply.setStatus(status);
        apply.setHandleMan(operator);
        apply.setHandleTime(LocalDateTime.now());
        apply.setHandleNote(note);
        if (status == 1) {
            apply.setRefundAmount(refundAmount == null ? apply.getRefundAmount() : refundAmount);
        }
        returnApplyMapper.updateById(apply);
        saveHistory(apply.getOrderId(), operator, null,
                status == 1 ? "退货申请已通过，退款金额：" + apply.getRefundAmount() : "退货申请已拒绝：" + note);
    }

    public List<OrderReturnReason> returnReasons() {
        return returnReasonMapper.selectList(new LambdaQueryWrapper<OrderReturnReason>()
                .orderByAsc(OrderReturnReason::getSort));
    }

    // ---------------- 订单设置 ----------------
    public OrderSetting orderSetting() {
        OrderSetting setting = orderSettingMapper.selectOne(new LambdaQueryWrapper<OrderSetting>().last("limit 1"));
        if (setting == null) {
            setting = new OrderSetting();
            setting.setFlashOrderOvertime(1800);
            setting.setNormalOrderOvertime(1800);
            setting.setConfirmOvertime(15);
            setting.setCommentOvertime(7);
            setting.setCreateTime(LocalDateTime.now());
            orderSettingMapper.insert(setting);
        }
        return setting;
    }

    public void updateOrderSetting(OrderSetting setting) {
        OrderSetting existing = orderSetting();
        setting.setId(existing.getId());
        orderSettingMapper.updateById(setting);
    }

    // ---------------- 首页统计 ----------------
    public Map<String, Object> dashboard() {
        Map<String, Object> result = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        result.put("totalOrders", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getDeleteStatus, 0)));
        result.put("todayOrders", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getDeleteStatus, 0).ge(Order::getCreateTime, todayStart)));
        result.put("unpaid", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getDeleteStatus, 0).eq(Order::getStatus, 0)));
        result.put("unshipped", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getDeleteStatus, 0).eq(Order::getStatus, 1)));
        result.put("shipped", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getDeleteStatus, 0).eq(Order::getStatus, 2)));
        result.put("returnPending", returnApplyMapper.selectCount(new LambdaQueryWrapper<OrderReturnApply>()
                .eq(OrderReturnApply::getStatus, 0)));
        List<Order> paidOrders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getDeleteStatus, 0)
                .in(Order::getStatus, 1, 2, 3));
        BigDecimal sales = paidOrders.stream().map(Order::getPayAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        result.put("totalSales", sales);
        result.put("latestOrders", orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getDeleteStatus, 0).orderByDesc(Order::getId).last("limit 6")));
        return result;
    }

    private void restoreStock(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
        for (OrderItem item : items) {
            skuStockMapper.unlockStock(item.getProductSkuId(), item.getProductQuantity());
            productMapper.unlockStock(item.getProductId(), item.getProductQuantity());
        }
    }

    private void saveHistory(Long orderId, String operator, Integer status, String note) {
        OrderOperateHistory history = new OrderOperateHistory();
        history.setOrderId(orderId);
        history.setOperateMan(operator);
        history.setCreateTime(LocalDateTime.now());
        history.setOrderStatus(status);
        history.setNote(note);
        historyMapper.insert(history);
    }
}
