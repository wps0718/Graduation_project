package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserBanDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Size(max = 200, message = "封禁原因不能超过200字")
    private String banReason;
}
