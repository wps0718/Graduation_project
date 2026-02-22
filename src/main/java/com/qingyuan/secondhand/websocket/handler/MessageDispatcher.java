package com.qingyuan.secondhand.websocket.handler;

import com.qingyuan.secondhand.websocket.protocol.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageDispatcher {

    private final PingMessageHandler pingMessageHandler;
    private final ChatMessageHandler chatMessageHandler;
    private final ReadMessageHandler readMessageHandler;

    public void dispatch(Long senderId, WebSocketMessage<?> message) {
        String type = message.getType().toUpperCase();

        switch (type) {
            case "PING":
                pingMessageHandler.handle(senderId);
                break;
            case "CHAT":
                chatMessageHandler.handle(senderId, message.getData());
                break;
            case "READ":
                readMessageHandler.handle(senderId, message.getData());
                break;
            default:
                log.warn("未知消息类型: {}, senderId={}", type, senderId);
        }
    }
}
