package com.qingyuan.secondhand.controller.common;

import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.common.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/common")
@RequiredArgsConstructor
public class CommonController {

    private final FileUtil fileUtil;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file, @RequestParam(value = "type", defaultValue = "common") String type) {
        String url = fileUtil.upload(file, type);
        return Result.success(Map.of("url", url));
    }
}
