package com.qingyuan.secondhand.service.impl;

import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.enums.NotificationType;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.common.util.SessionKeyUtil;
import com.qingyuan.secondhand.entity.ChatMessage;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.ChatMessageMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.ChatMessageService;
import com.qingyuan.secondhand.service.ChatSessionService;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.vo.ChatMessageVO;
import com.qingyuan.secondhand.websocket.WebSocketSessionManager;
import com.qingyuan.secondhand.websocket.protocol.ChatPayload;
import com.qingyuan.secondhand.websocket.protocol.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatSessionService chatSessionService;
    private final WebSocketSessionManager sessionManager;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public Long saveAndPushMessage(Long senderId, ChatPayload payload) {
        if (senderId == null) {
            throw new BusinessException("发送者不能为空");
        }
        if (payload == null || payload.getReceiverId() == null) {
            throw new BusinessException("接收者不能为空");
        }
        if (!StringUtils.hasText(payload.getContent())) {
            throw new BusinessException("消息内容不能为空");
        }
        if (payload.getContent().length() > 1000) {
            throw new BusinessException("消息内容不能超过1000字符");
        }
        if (payload.getMsgType() == null || payload.getMsgType() < 1 || payload.getMsgType() > 5) {
            throw new BusinessException("消息类型错误");
        }
        if (senderId.equals(payload.getReceiverId())) {
            throw new BusinessException("不能给自己发消息");
        }

        String sessionKey = SessionKeyUtil.buildSessionKey(senderId, payload.getReceiverId(), payload.getProductId());
        chatSessionService.ensureSessionExists(senderId, payload.getReceiverId(), payload.getProductId());

        ChatMessage message = buildMessage(senderId, payload, sessionKey);
        int inserted = chatMessageMapper.insert(message);
        if (inserted <= 0) {
            throw new BusinessException("消息保存失败");
        }

        String lastMsg = calculateLastMsg(payload);
        chatSessionService.updateSessionLastMsg(senderId, payload.getReceiverId(), payload.getProductId(), lastMsg, payload.getMsgType());
        chatSessionService.incrementUnread(payload.getReceiverId(), senderId, payload.getProductId());
        stringRedisTemplate.opsForValue().increment(RedisConstant.IM_UNREAD + payload.getReceiverId());

        Map<String, Object> pushData = buildPushData(senderId, payload, sessionKey, message);
        WebSocketMessage<Map<String, Object>> wsMsg = new WebSocketMessage<>("CHAT", pushData);
        boolean sent = sessionManager.sendToUser(payload.getReceiverId(), wsMsg);

        if (!sent) {
            sendOfflineNotification(senderId, payload.getReceiverId(), payload, message, lastMsg);
        }

        return message.getId();
    }

    @Override
    public Map<String, Object> getMessageHistory(String sessionKey, Integer page, Integer pageSize) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("未登录");
        }
        if (!StringUtils.hasText(sessionKey)) {
            throw new BusinessException("sessionKey不能为空");
        }
        if (page == null || pageSize == null) {
            throw new BusinessException("分页参数不能为空");
        }
        if (page <= 0 || pageSize <= 0) {
            throw new BusinessException("分页参数不正确");
        }
        if (!SessionKeyUtil.isParticipant(sessionKey, currentUserId)) {
            throw new BusinessException("无权查看此会话");
        }

        int offset = (page - 1) * pageSize;
        int total = chatMessageMapper.selectMessageCount(sessionKey);
        List<ChatMessage> messages = chatMessageMapper.selectMessagesBySessionKey(sessionKey, offset, pageSize);

        List<ChatMessageVO> voList = messages.stream()
                .map(msg -> ChatMessageVO.builder()
                        .msgId(msg.getId())
                        .senderId(msg.getSenderId())
                        .receiverId(msg.getReceiverId())
                        .msgType(msg.getMsgType())
                        .content(msg.getContent())
                        .productId(msg.getProductId())
                        .orderId(msg.getOrderId())
                        .isRead(Integer.valueOf(1).equals(msg.getIsRead()))
                        .isSelf(msg.getSenderId() != null && msg.getSenderId().equals(currentUserId))
                        .createTime(msg.getCreateTime())
                        .build())
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("records", voList);
        return result;
    }

    @Override
    public void markSessionRead(String sessionKey) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("未登录");
        }
        markSessionReadByUserId(currentUserId, sessionKey);
    }

    @Override
    public void markSessionReadByUserId(Long userId, String sessionKey) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (!StringUtils.hasText(sessionKey)) {
            throw new BusinessException("sessionKey不能为空");
        }
        if (!SessionKeyUtil.isParticipant(sessionKey, userId)) {
            throw new BusinessException("无权操作此会话");
        }

        chatMessageMapper.markAsRead(userId, sessionKey);
        Long peerId = SessionKeyUtil.getPeerId(sessionKey, userId);
        Long productId = SessionKeyUtil.getProductId(sessionKey);
        if (peerId == null) {
            throw new BusinessException("会话信息不完整");
        }
        chatSessionService.resetUnread(userId, peerId, productId);
        Integer newTotal = chatSessionService.calculateUnreadTotal(userId);
        stringRedisTemplate.opsForValue().set(RedisConstant.IM_UNREAD + userId, String.valueOf(newTotal));
    }

    @Override
    public void sendSystemMessage(String sessionKey, Long senderId, Long receiverId, Integer msgType, String content) {
        if (!StringUtils.hasText(sessionKey)) {
            throw new BusinessException("sessionKey不能为空");
        }
        if (senderId == null || receiverId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (msgType == null) {
            throw new BusinessException("消息类型不能为空");
        }
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("消息内容不能为空");
        }

        ChatMessage message = ChatMessage.builder()
                .sessionKey(sessionKey)
                .senderId(senderId)
                .receiverId(receiverId)
                .msgType(msgType)
                .content(content)
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();
        int inserted = chatMessageMapper.insert(message);
        if (inserted <= 0) {
            throw new BusinessException("消息保存失败");
        }

        String lastMsg = content.length() > 50 ? content.substring(0, 50) + "..." : content;
        chatSessionService.updateSessionLastMsg(senderId, receiverId, null, lastMsg, msgType);
        chatSessionService.incrementUnread(receiverId, senderId, null);
        stringRedisTemplate.opsForValue().increment(RedisConstant.IM_UNREAD + receiverId);

        Map<String, Object> pushData = new HashMap<>();
        pushData.put("msgId", message.getId());
        pushData.put("sessionKey", sessionKey);
        pushData.put("senderId", senderId);
        pushData.put("receiverId", receiverId);
        pushData.put("msgType", msgType);
        pushData.put("content", content);
        pushData.put("createTime", message.getCreateTime());
        WebSocketMessage<Map<String, Object>> wsMsg = new WebSocketMessage<>("SYSTEM", pushData);
        sessionManager.sendToUser(receiverId, wsMsg);
    }

    private ChatMessage buildMessage(Long senderId, ChatPayload payload, String sessionKey) {
        return ChatMessage.builder()
                .sessionKey(sessionKey)
                .senderId(senderId)
                .receiverId(payload.getReceiverId())
                .msgType(payload.getMsgType())
                .content(payload.getContent())
                .productId(payload.getProductId())
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();
    }

    private String calculateLastMsg(ChatPayload payload) {
        if (Integer.valueOf(2).equals(payload.getMsgType())) {
            return "[商品卡片]";
        }
        if (Integer.valueOf(3).equals(payload.getMsgType())) {
            return "[订单信息]";
        }
        String content = payload.getContent();
        return content.length() > 50 ? content.substring(0, 50) + "..." : content;
    }

    private Map<String, Object> buildPushData(Long senderId, ChatPayload payload, String sessionKey, ChatMessage message) {
        User sender = userMapper.selectById(senderId);
        if (sender == null) {
            throw new BusinessException("发送者不存在");
        }
        Map<String, Object> pushData = new HashMap<>();
        pushData.put("msgId", message.getId());
        pushData.put("sessionKey", sessionKey);
        pushData.put("senderId", senderId);
        pushData.put("senderName", sender.getNickName());
        pushData.put("senderAvatar", sender.getAvatarUrl());
        pushData.put("receiverId", payload.getReceiverId());
        pushData.put("productId", payload.getProductId());
        pushData.put("msgType", payload.getMsgType());
        pushData.put("content", payload.getContent());
        pushData.put("createTime", message.getCreateTime());
        return pushData;
    }

    private void sendOfflineNotification(Long senderId, Long receiverId, ChatPayload payload, ChatMessage message, String lastMsg) {
        User sender = userMapper.selectById(senderId);
        if (sender == null) {
            throw new BusinessException("发送者不存在");
        }
        String preview = buildMessagePreview(payload, lastMsg);
        Map<String, String> params = new HashMap<>();
        params.put("nickName", sender.getNickName());
        params.put("content", preview);
        notificationService.send(
                receiverId,
                NotificationType.NEW_MESSAGE,
                params,
                message.getId(),
                1,
                1
        );
        log.info("接收方{}离线，已写入notification", receiverId);
    }

    private String buildMessagePreview(ChatPayload payload, String lastMsg) {
        String source = payload != null && StringUtils.hasText(payload.getContent()) ? payload.getContent() : lastMsg;
        if (!StringUtils.hasText(source)) {
            return "";
        }
        if (source.length() > 20) {
            return source.substring(0, 20) + "...";
        }
        return source;
    }
}
