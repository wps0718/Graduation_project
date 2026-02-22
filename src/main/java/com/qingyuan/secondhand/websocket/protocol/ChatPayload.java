package com.qingyuan.secondhand.websocket.protocol;

import lombok.Data;

@Data
public class ChatPayload {
    private Long receiverId;
    private Long productId;
    private Integer msgType;
    private String content;
}
