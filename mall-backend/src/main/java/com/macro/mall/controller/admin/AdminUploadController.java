package com.macro.mall.controller.admin;

import com.macro.mall.common.CommonResult;
import com.macro.mall.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/oss")
@RequiredArgsConstructor
public class AdminUploadController {
    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public CommonResult<String> upload(@RequestParam("file") MultipartFile file) {
        return CommonResult.success(fileStorageService.store(file));
    }
}
