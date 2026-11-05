package com.macro.mall.controller.admin;

import com.macro.mall.auth.AuthContext;
import com.macro.mall.common.CommonPage;
import com.macro.mall.common.CommonResult;
import com.macro.mall.dto.ReturnHandleParam;
import com.macro.mall.entity.OrderReturnApply;
import com.macro.mall.entity.OrderReturnReason;
import com.macro.mall.mapper.OrderReturnReasonMapper;
import com.macro.mall.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class OmsReturnController {
    private final AdminOrderService adminOrderService;
    private final OrderReturnReasonMapper reasonMapper;

    @GetMapping("/returnApply/list")
    public CommonResult<CommonPage<OrderReturnApply>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return CommonResult.success(CommonPage.of(adminOrderService.returnApplies(pageNum, pageSize, status, keyword)));
    }

    @GetMapping("/returnApply/{id}")
    public CommonResult<OrderReturnApply> detail(@PathVariable Long id) {
        return CommonResult.success(adminOrderService.returnApplyDetail(id));
    }

    @PostMapping("/returnApply/handle")
    public CommonResult<Void> handle(@RequestBody ReturnHandleParam param) {
        adminOrderService.handleReturn(param.getId(), param.getStatus(), param.getNote(),
                param.getRefundAmount(), "admin:" + AuthContext.requireAdminId());
        return CommonResult.success();
    }

    @GetMapping("/returnReason/list")
    public CommonResult<List<OrderReturnReason>> reasonList() {
        return CommonResult.success(adminOrderService.returnReasons());
    }

    @PostMapping("/returnReason/create")
    public CommonResult<Void> createReason(@RequestBody OrderReturnReason reason) {
        if (reason.getName() == null || reason.getName().isBlank()) {
            return CommonResult.failed("原因名称不能为空");
        }
        if (reason.getStatus() == null) {
            reason.setStatus(1);
        }
        reasonMapper.insert(reason);
        return CommonResult.success();
    }

    @PostMapping("/returnReason/update")
    public CommonResult<Void> updateReason(@RequestBody OrderReturnReason reason) {
        reasonMapper.updateById(reason);
        return CommonResult.success();
    }

    @PostMapping("/returnReason/delete/{id}")
    public CommonResult<Void> deleteReason(@PathVariable Long id) {
        reasonMapper.deleteById(id);
        return CommonResult.success();
    }
}
