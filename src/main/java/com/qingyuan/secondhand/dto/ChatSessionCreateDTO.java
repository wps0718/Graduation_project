package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatSessionCreateDTO {
    @NotNull(message = "对方用户ID不能为空")
    private Long peerId;
    private Long productId;
}
