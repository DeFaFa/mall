package com.macro.mall.controller.admin;

import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.Menu;
import com.macro.mall.service.RbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/menu")
@RequiredArgsConstructor
public class UmsMenuController {
    private final RbacService rbacService;

    @GetMapping("/tree")
    public CommonResult<List<Menu>> tree() {
        return CommonResult.success(rbacService.menuTree());
    }

    @PostMapping("/create")
    public CommonResult<Void> create(@RequestBody Menu menu) {
        rbacService.createMenu(menu);
        return CommonResult.success();
    }

    @PostMapping("/update")
    public CommonResult<Void> update(@RequestBody Menu menu) {
        rbacService.updateMenu(menu);
        return CommonResult.success();
    }

    @PostMapping("/delete/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        rbacService.deleteMenu(id);
        return CommonResult.success();
    }
}
