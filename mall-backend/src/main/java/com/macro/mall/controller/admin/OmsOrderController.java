package com.macro.mall.controller.admin;

import com.macro.mall.auth.AuthContext;
import com.macro.mall.common.CommonPage;
import com.macro.mall.common.CommonResult;
import com.macro.mall.dto.*;
import com.macro.mall.entity.Order;
import com.macro.mall.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class OmsOrderController {
    private final AdminOrderService adminOrderService;

    @GetMapping("/list")
    public CommonResult<CommonPage<Order>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String orderSn,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer sourceType,
            @RequestParam(required = false) Integer orderType,
            @RequestParam(required = false) String receiverKeyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return CommonResult.success(CommonPage.of(adminOrderService.page(pageNum, pageSize, orderSn, status,
                sourceType, orderType, receiverKeyword, startDate, endDate)));
    }

    @GetMapping("/{id}")
    public CommonResult<OrderDetail> detail(@PathVariable Long id) {
        return CommonResult.success(adminOrderService.detail(id));
    }

    @PostMapping("/delivery")
    public CommonResult<Void> delivery(@RequestBody DeliveryParam param) {
        adminOrderService.delivery(param.getIds(), param.getDeliveryCompany(), param.getDeliverySn());
        return CommonResult.success();
    }

    @PostMapping("/close")
    public CommonResult<Void> close(@RequestBody List<Long> ids) {
        adminOrderService.close(ids, operator());
        return CommonResult.success();
    }

    @PostMapping("/delete")
    public CommonResult<Void> delete(@RequestBody List<Long> ids) {
        adminOrderService.logicalDelete(ids);
        return CommonResult.success();
    }

    @PostMapping("/updateReceiver")
    public CommonResult<Void> updateReceiver(@RequestBody OrderReceiverParam param) {
        Order order = new Order();
        order.setReceiverName(param.getReceiverName());
        order.setReceiverPhone(param.getReceiverPhone());
        order.setReceiverProvince(param.getReceiverProvince());
        order.setReceiverCity(param.getReceiverCity());
        order.setReceiverRegion(param.getReceiverRegion());
        order.setReceiverDetailAddress(param.getReceiverDetailAddress());
        adminOrderService.updateReceiver(param.getOrderId(), order, operator());
        return CommonResult.success();
    }

    @PostMapping("/updateMoney")
    public CommonResult<Void> updateMoney(@RequestBody OrderMoneyParam param) {
        adminOrderService.updateMoney(param.getOrderId(), param.getFreightAmount(),
                param.getDiscountAmount(), operator());
        return CommonResult.success();
    }

    @PostMapping("/updateNote")
    public CommonResult<Void> updateNote(@RequestBody OrderNoteParam param) {
        adminOrderService.updateNote(param.getOrderId(), param.getNote(), operator());
        return CommonResult.success();
    }

    private String operator() {
        return "admin:" + AuthContext.requireAdminId();
    }
}
