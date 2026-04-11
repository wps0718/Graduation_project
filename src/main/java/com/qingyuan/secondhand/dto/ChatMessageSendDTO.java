package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatMessageSendDTO {
    @NotBlank(message = "sessionKey不能为空")
    private String sessionKey;

    @NotNull(message = "消息类型不能为空")
    private Integer type;

    @NotBlank(message = "消息内容不能为空")
    private String content;
}
