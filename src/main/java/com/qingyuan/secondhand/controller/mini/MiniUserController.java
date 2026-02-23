package com.qingyuan.secondhand.controller.mini;

import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.AccountLoginDTO;
import com.qingyuan.secondhand.dto.SmsLoginDTO;
import com.qingyuan.secondhand.dto.SmsSendDTO;
import com.qingyuan.secondhand.dto.UserUpdateDTO;
import com.qingyuan.secondhand.dto.WxLoginDTO;
import com.qingyuan.secondhand.service.UserService;
import com.qingyuan.secondhand.vo.LoginVO;
import com.qingyuan.secondhand.vo.UserInfoVO;
import com.qingyuan.secondhand.vo.UserProfileVO;
import com.qingyuan.secondhand.vo.UserStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/mini/user")
@RequiredArgsConstructor
public class MiniUserController {

    private final UserService userService;

    @PostMapping("/wx-login")
    public Result<LoginVO> wxLogin(@RequestBody @Valid WxLoginDTO dto) {
        LoginVO vo = userService.wxLogin(dto);
        return Result.success(vo);
    }

    @PostMapping("/login")
    public Result<LoginVO> accountLogin(@RequestBody @Valid AccountLoginDTO dto) {
        LoginVO vo = userService.accountLogin(dto);
        return Result.success(vo);
    }

    @PostMapping("/sms/send")
    public Result<Void> sendSmsCode(@RequestBody @Valid SmsSendDTO dto) {
        userService.sendSmsCode(dto);
        return Result.success();
    }

    @PostMapping("/sms-login")
    public Result<LoginVO> smsLogin(@RequestBody @Valid SmsLoginDTO dto) {
        LoginVO vo = userService.smsLogin(dto);
        return Result.success(vo);
    }

    @GetMapping("/info")
    public Result<UserInfoVO> getUserInfo() {
        return Result.success(userService.getUserInfo());
    }

    @PostMapping("/update")
    public Result<Void> updateUserInfo(@RequestBody @Valid UserUpdateDTO dto) {
        userService.updateUserInfo(dto);
        return Result.success();
    }

    @PostMapping("/accept-agreement")
    public Result<Void> acceptAgreement() {
        userService.acceptAgreement();
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<UserStatsVO> getUserStats() {
        return Result.success(userService.getUserStats());
    }

    @GetMapping("/profile/{id}")
    public Result<UserProfileVO> getUserProfile(@PathVariable Long id,
                                                @RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer pageSize) {
        UserProfileVO vo = userService.getUserProfile(id, page, pageSize);
        return Result.success(vo);
    }

    @PostMapping("/deactivate")
    public Result<Void> deactivateAccount() {
        userService.deactivateAccount();
        return Result.success();
    }

    @PostMapping("/restore")
    public Result<Void> restoreAccount() {
        userService.restoreAccount();
        return Result.success();
    }
}
