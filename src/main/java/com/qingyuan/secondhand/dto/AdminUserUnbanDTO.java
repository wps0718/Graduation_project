package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserUnbanDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
