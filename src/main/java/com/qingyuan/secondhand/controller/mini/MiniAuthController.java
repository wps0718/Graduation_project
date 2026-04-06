package com.qingyuan.secondhand.controller.mini;

import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.AuthSubmitDTO;
import com.qingyuan.secondhand.service.CampusAuthService;
import com.qingyuan.secondhand.vo.AuthHistoryVO;
import com.qingyuan.secondhand.vo.AuthStatusVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mini/auth")
@RequiredArgsConstructor
public class MiniAuthController {

    private final CampusAuthService campusAuthService;

    @PostMapping("/submit")
    public Result<Void> submitAuth(@RequestBody @Valid AuthSubmitDTO dto) {
        campusAuthService.submitAuth(dto);
        return Result.success();
    }

    @GetMapping("/status")
    public Result<AuthStatusVO> getAuthStatus() {
        AuthStatusVO vo = campusAuthService.getAuthStatus();
        return Result.success(vo);
    }

    @GetMapping("/history")
    public Result<List<AuthHistoryVO>> listAuthHistory() {
        return Result.success(campusAuthService.listAuthHistory());
    }

    @GetMapping("/history/{id}")
    public Result<AuthHistoryVO> getAuthHistoryDetail(@PathVariable Long id) {
        return Result.success(campusAuthService.getAuthHistoryDetail(id));
    }
}
