package com.macro.mall.controller.admin;

import com.macro.mall.common.CommonPage;
import com.macro.mall.common.CommonResult;
import com.macro.mall.dto.AdminParam;
import com.macro.mall.entity.Admin;
import com.macro.mall.service.RbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/admin")
@RequiredArgsConstructor
public class UmsAdminController {
    private final RbacService rbacService;

    @GetMapping("/list")
    public CommonResult<CommonPage<Admin>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return CommonResult.success(CommonPage.of(rbacService.adminPage(pageNum, pageSize, keyword)));
    }

    @GetMapping("/{id}")
    public CommonResult<Map<String, Object>> detail(@PathVariable Long id) {
        return CommonResult.success(rbacService.adminDetail(id));
    }

    @PostMapping("/create")
    public CommonResult<Void> create(@RequestBody AdminParam param) {
        rbacService.createAdmin(param);
        return CommonResult.success();
    }

    @PostMapping("/update/{id}")
    public CommonResult<Void> update(@PathVariable Long id, @RequestBody AdminParam param) {
        rbacService.updateAdmin(id, param);
        return CommonResult.success();
    }

    @PostMapping("/delete")
    public CommonResult<Void> delete(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            rbacService.deleteAdmin(id);
        }
        return CommonResult.success();
    }
}
