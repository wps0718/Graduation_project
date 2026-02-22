package com.qingyuan.secondhand.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.common.util.SessionKeyUtil;
import com.qingyuan.secondhand.service.ChatMessageService;
import com.qingyuan.secondhand.websocket.WebSocketSessionManager;
import com.qingyuan.secondhand.websocket.protocol.ReadPayload;
import com.qingyuan.secondhand.websocket.protocol.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReadMessageHandler {

    private final ChatMessageService chatMessageService;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public void handle(Long senderId, Object data) {
        try {
            if (senderId == null) {
                throw new BusinessException("发送者不能为空");
            }
            if (data == null) {
                throw new BusinessException("消息数据不能为空");
            }
            ReadPayload payload = objectMapper.convertValue(data, ReadPayload.class);
            if (payload == null || payload.getSessionKey() == null) {
                throw new BusinessException("sessionKey不能为空");
            }
            chatMessageService.markSessionReadByUserId(senderId, payload.getSessionKey());
            Long peerId = SessionKeyUtil.getPeerId(payload.getSessionKey(), senderId);
            if (peerId != null) {
                Map<String, Object> ackData = new HashMap<>();
                ackData.put("sessionKey", payload.getSessionKey());
                ackData.put("readerId", senderId);
                WebSocketMessage<Map<String, Object>> ackMsg = new WebSocketMessage<>("READ_ACK", ackData);
                sessionManager.sendToUser(peerId, ackMsg);
            }
            log.info("用户{}标记会话{}已读", senderId, payload.getSessionKey());
        } catch (BusinessException e) {
            log.warn("用户{}标记已读业务异常: {}", senderId, e.getMessage());
        } catch (Exception e) {
            log.error("用户{}标记已读异常", senderId, e);
        }
    }
}
