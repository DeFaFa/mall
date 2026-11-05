package com.macro.mall.dto;

import com.macro.mall.entity.Order;
import com.macro.mall.entity.OrderItem;
import com.macro.mall.entity.OrderOperateHistory;
import com.macro.mall.entity.OrderReturnApply;
import lombok.Data;

import java.util.List;

@Data
public class OrderDetail {
    private Order order;
    private List<OrderItem> orderItemList;
    private List<OrderOperateHistory> historyList;
    private OrderReturnApply returnApply;
}
