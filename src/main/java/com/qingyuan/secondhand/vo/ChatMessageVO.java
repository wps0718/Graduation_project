package com.qingyuan.secondhand.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVO {
    private Long msgId;
    private Long senderId;
    private Long receiverId;
    private Integer msgType;
    private String content;
    private Long productId;
    private Long orderId;
    private Boolean isRead;
    private Boolean isSelf;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
