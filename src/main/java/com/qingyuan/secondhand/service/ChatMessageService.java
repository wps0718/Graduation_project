package com.qingyuan.secondhand.service;

import com.qingyuan.secondhand.websocket.protocol.ChatPayload;

import java.util.Map;

public interface ChatMessageService {
    Long saveAndPushMessage(Long senderId, ChatPayload payload);

    Map<String, Object> getMessageHistory(String sessionKey, Integer page, Integer pageSize);

    void markSessionRead(String sessionKey);

    void markSessionReadByUserId(Long userId, String sessionKey);

    void sendSystemMessage(String sessionKey, Long senderId, Long receiverId, Integer msgType, String content);

    Long sendMessage(com.qingyuan.secondhand.dto.ChatMessageSendDTO dto);
}
