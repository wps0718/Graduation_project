package com.qingyuan.secondhand.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.AdminUserBanDTO;
import com.qingyuan.secondhand.dto.AdminUserUnbanDTO;
import com.qingyuan.secondhand.service.UserService;
import com.qingyuan.secondhand.vo.AdminUserDetailVO;
import com.qingyuan.secondhand.vo.AdminUserPageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping("/page")
    public Result<Page<AdminUserPageVO>> page(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) Integer authStatus,
                                              @RequestParam(required = false) Long campusId) {
        return Result.success(userService.getAdminUserPage(page, pageSize, keyword, status, authStatus, campusId));
    }

    @GetMapping("/detail/{id}")
    public Result<AdminUserDetailVO> detail(@PathVariable Long id) {
        return Result.success(userService.getAdminUserDetail(id));
    }

    @PostMapping("/ban")
    public Result<Void> ban(@Valid @RequestBody AdminUserBanDTO dto) {
        userService.banUser(dto.getUserId(), dto.getBanReason());
        return Result.success();
    }

    @PostMapping("/unban")
    public Result<Void> unban(@Valid @RequestBody AdminUserUnbanDTO dto) {
        userService.unbanUser(dto.getUserId());
        return Result.success();
    }
}
