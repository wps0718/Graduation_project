package com.qingyuan.secondhand.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.qingyuan.secondhand.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class FileUtil {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.url-prefix}")
    private String urlPrefix;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_TYPES = Set.of("common", "product", "auth", "avatar", "banner");

    public String upload(MultipartFile file, String type) {
        if (file.isEmpty()) {
            throw new BusinessException("File cannot be empty");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("File size exceeds 5MB limit");
        }

        String safeType = sanitizeType(type);

        String originalFilename = file.getOriginalFilename();
        String extension = cn.hutool.core.io.FileUtil.extName(originalFilename).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Only JPG, JPEG, PNG formats are allowed");
        }

        String uuid = IdUtil.simpleUUID();
        String datePath = DateUtil.format(new Date(), "yyyy/MM/dd");
        String fileName = uuid + "." + extension;
        
        String relativePath = safeType + "/" + datePath + "/";
        Path targetDir = Paths.get(uploadPath).toAbsolutePath().normalize().resolve(relativePath);
        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            throw new BusinessException("File upload failed: " + e.getMessage());
        }

        try {
            file.transferTo(targetDir.resolve(fileName).toFile());
        } catch (IOException e) {
            throw new BusinessException("File upload failed: " + e.getMessage());
        }

        // Return URL path, e.g. /uploads/product/2023/10/01/uuid.jpg
        return urlPrefix + relativePath + fileName;
    }

    private String sanitizeType(String type) {
        String value = type == null ? "" : type.trim();
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        if (value.isBlank()) {
            value = "common";
        }
        if (!ALLOWED_TYPES.contains(value)) {
            throw new BusinessException("Invalid upload type");
        }
        return value;
    }
}
