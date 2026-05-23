package com.qingyuan.secondhand.controller.admin;

import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.common.util.FileUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "管理端-公共接口")
@RestController
@RequestMapping("/admin/common")
@RequiredArgsConstructor
public class AdminCommonController {

    private final FileUtil fileUtil;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file, @RequestParam(value = "type", defaultValue = "common") String type) {
        String url = fileUtil.upload(file, type);
        return Result.success(Map.of("url", url));
    }
}
