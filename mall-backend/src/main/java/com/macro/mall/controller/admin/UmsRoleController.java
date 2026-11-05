package com.macro.mall.controller.admin;

import com.macro.mall.common.CommonResult;
import com.macro.mall.dto.RoleParam;
import com.macro.mall.entity.Role;
import com.macro.mall.service.RbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/role")
@RequiredArgsConstructor
public class UmsRoleController {
    private final RbacService rbacService;

    @GetMapping("/list")
    public CommonResult<List<Role>> list(@RequestParam(required = false) String keyword) {
        return CommonResult.success(rbacService.roles(keyword));
    }

    @GetMapping("/{id}")
    public CommonResult<RoleParam> detail(@PathVariable Long id) {
        return CommonResult.success(rbacService.roleDetail(id));
    }

    @PostMapping("/create")
    public CommonResult<Void> create(@RequestBody RoleParam param) {
        rbacService.createRole(param);
        return CommonResult.success();
    }

    @PostMapping("/update/{id}")
    public CommonResult<Void> update(@PathVariable Long id, @RequestBody RoleParam param) {
        rbacService.updateRole(id, param);
        return CommonResult.success();
    }

    @PostMapping("/delete")
    public CommonResult<Void> delete(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            rbacService.deleteRole(id);
        }
        return CommonResult.success();
    }
}
