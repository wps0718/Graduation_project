package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FollowUserDTO {
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}

