package com.qingyuan.secondhand.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.service.ChatMessageService;
import com.qingyuan.secondhand.websocket.WebSocketSessionManager;
import com.qingyuan.secondhand.websocket.protocol.ChatPayload;
import com.qingyuan.secondhand.websocket.protocol.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatMessageHandler {

    private final ChatMessageService chatMessageService;
    private final ObjectMapper objectMapper;
    private final WebSocketSessionManager sessionManager;

    public void handle(Long senderId, Object data) {
        try {
            if (senderId == null) {
                throw new BusinessException("发送者不能为空");
            }
            if (data == null) {
                throw new BusinessException("消息数据不能为空");
            }
            ChatPayload payload = objectMapper.convertValue(data, ChatPayload.class);
            chatMessageService.saveAndPushMessage(senderId, payload);
            log.info("用户{}发送消息成功，receiverId={}", senderId, payload.getReceiverId());
        } catch (BusinessException e) {
            log.warn("用户{}发送消息业务异常: {}", senderId, e.getMessage());
            Map<String, String> errorData = Map.of("error", e.getMessage());
            WebSocketMessage<Map<String, String>> errorMsg = new WebSocketMessage<>("SYSTEM", errorData);
            sessionManager.sendToUser(senderId, errorMsg);
        } catch (Exception e) {
            log.error("用户{}发送消息异常", senderId, e);
            Map<String, String> errorData = Map.of("error", "消息发送失败");
            WebSocketMessage<Map<String, String>> errorMsg = new WebSocketMessage<>("SYSTEM", errorData);
            sessionManager.sendToUser(senderId, errorMsg);
        }
    }
}
