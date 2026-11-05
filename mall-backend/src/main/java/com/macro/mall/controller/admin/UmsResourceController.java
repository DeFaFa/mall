package com.macro.mall.controller.admin;

import com.macro.mall.common.CommonPage;
import com.macro.mall.common.CommonResult;
import com.macro.mall.entity.Resource;
import com.macro.mall.service.RbacService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/resource")
@RequiredArgsConstructor
public class UmsResourceController {
    private final RbacService rbacService;

    @GetMapping("/list")
    public CommonResult<CommonPage<Resource>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return CommonResult.success(CommonPage.of(rbacService.resourcePage(pageNum, pageSize, keyword)));
    }

    @GetMapping("/all")
    public CommonResult<List<Resource>> all() {
        return CommonResult.success(rbacService.allResources());
    }

    @PostMapping("/create")
    public CommonResult<Void> create(@RequestBody Resource resource) {
        rbacService.createResource(resource);
        return CommonResult.success();
    }

    @PostMapping("/update")
    public CommonResult<Void> update(@RequestBody Resource resource) {
        rbacService.updateResource(resource);
        return CommonResult.success();
    }

    @PostMapping("/delete/{id}")
    public CommonResult<Void> delete(@PathVariable Long id) {
        rbacService.deleteResource(id);
        return CommonResult.success();
    }
}
