package com.txy.controller;


import com.txy.log.ApiLog;
import com.txy.util.CommonUtil;
import com.txy.vo.PlayerVo;
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
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController extends BaseController{

    @Value("${upload.image.path}")
    private String uploadPath;

    @Value("${upload.image.domain}")
    private String domain;
    @ApiLog("图片上传接口")
    @PostMapping("/image")
    public UploadResult uploadImage(@RequestParam("file") MultipartFile file,@RequestHeader("sessionId") String sessionId) throws IOException {
        PlayerVo playerVo = getCurrentBuyerVo( sessionId);
        if (CommonUtil.isNull(playerVo)){
            return null;
        }
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