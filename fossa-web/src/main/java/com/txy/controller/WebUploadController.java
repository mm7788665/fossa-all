package com.txy.controller;


import com.txy.log.ApiLog;
import com.txy.security.RequirePerm;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Created by huyawei 2026/9/12 20:49
 *
 * ⚠️ 路径改动：原来是 /web/upload，不在 /api/** 下，拦截器管不到，
 *    任何人不用登录就能上传文件（会被塞垃圾、占满磁盘）。
 *    已改为 /api/upload，纳入登录校验。
 *
 *    如果前端已经在调 /web/upload/image，二选一：
 *      ① 前端同步改成 /api/upload/image（推荐）
 *      ② 保留旧路径：在 WebMvcConfig 里多加一条 .addPathPatterns("/web/**")
 *
 * 权限：upload:edit（超级管理员 + 运营编辑可用，只读访客不可用）。
 * 对应权限点已加在 01_权限与审计建表.sql 里。
 */
@RestController
@RequestMapping("/api/upload")
public class WebUploadController extends BaseController {

    @Value("${upload.image.path}")
    private String uploadPath;

    @Value("${upload.image.domain}")
    private String domain;

    @ApiLog("图片上传接口")
    @RequirePerm(value = "upload:edit", module = "upload", desc = "上传图片")
    @PostMapping("/image")
    public UploadResult uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        // 1. 校验
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("仅支持图片文件");
        }

        // 2. 生成文件名
        String suffix = getSuffix(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + suffix;

        // 3. 按日期分目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        File dir = new File(uploadPath, dateDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File destFile = new File(dir, fileName);
        File parentDir = destFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
            // 目录：其他用户可读+可进入
            parentDir.setReadable(true, false);
            parentDir.setExecutable(true, false);
        }
        file.transferTo(destFile);
        // 文件：其他用户可读
        destFile.setReadable(true, false);

        // 5. 返回访问地址
        String url = domain + "/upload/" + dateDir + "/" + fileName;
        return new UploadResult(url);
    }

    private String getSuffix(String filename) {
        if (filename == null) return ".jpg";
        int index = filename.lastIndexOf('.');
        return index == -1 ? ".jpg" : filename.substring(index);
    }

    @Data
    @AllArgsConstructor
    public static class UploadResult {
        private String url;
    }
}
