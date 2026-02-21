package com.qingyuan.secondhand.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.service.CampusAuthService;
import com.qingyuan.secondhand.vo.AuthPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final CampusAuthService campusAuthService;

    @GetMapping("/page")
    public Result<Page<AuthPageVO>> pageAuth(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long collegeId) {
        Page<AuthPageVO> result = campusAuthService.pageAuth(page, size, status, collegeId);
        return Result.success(result);
    }

    @GetMapping("/detail/{id}")
    public Result<AuthPageVO> getAuthDetail(@PathVariable Long id) {
        AuthPageVO vo = campusAuthService.getAuthDetail(id);
        return Result.success(vo);
    }

    @PostMapping("/approve")
    public Result<Void> approveAuth(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        if (id == null) {
            throw new BusinessException("认证记录ID不能为空");
        }
        campusAuthService.approveAuth(id);
        return Result.success();
    }

    @PostMapping("/reject")
    public Result<Void> rejectAuth(@RequestBody Map<String, Object> params) {
        Long id = params.get("id") == null ? null : Long.valueOf(params.get("id").toString());
        String rejectReason = params.get("rejectReason") == null ? null : params.get("rejectReason").toString();
        if (id == null) {
            throw new BusinessException("认证记录ID不能为空");
        }
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            throw new BusinessException("驳回原因不能为空");
        }
        campusAuthService.rejectAuth(id, rejectReason);
        return Result.success();
    }
}
