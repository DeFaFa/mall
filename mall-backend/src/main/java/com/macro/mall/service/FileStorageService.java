package com.macro.mall.service;

import com.macro.mall.common.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path root;
    private final String baseUrl;

    public FileStorageService(@Value("${mall.upload-dir:./uploads}") String uploadDir,
                              @Value("${mall.file-base-url:http://localhost:8080}") String baseUrl) {
        this.root = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建上传目录", e);
        }
    }

    /**
     * 本地存储实现，接口形态与 OSS/MinIO 一致：上传后返回可访问 URL。
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) {
            ext = original.substring(dot).toLowerCase();
        }
        if (!ext.matches("\\.(jpg|jpeg|png|gif|webp|svg)")) {
            throw new BizException("只支持图片格式上传");
        }
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        try {
            Files.copy(file.getInputStream(), root.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BizException("文件保存失败");
        }
        return baseUrl + "/uploads/" + name;
    }
}
