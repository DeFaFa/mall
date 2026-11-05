package com.macro.mall.controller.portal;

import com.macro.mall.common.CommonResult;
import com.macro.mall.dto.HomeResult;
import com.macro.mall.service.PortalHomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/portal/home")
@RequiredArgsConstructor
public class PortalHomeController {
    private final PortalHomeService portalHomeService;

    @GetMapping("/index")
    public CommonResult<HomeResult> index() {
        return CommonResult.success(portalHomeService.index());
    }
}
