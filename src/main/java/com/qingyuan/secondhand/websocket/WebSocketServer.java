package com.qingyuan.secondhand.websocket;

import com.qingyuan.secondhand.websocket.handler.MessageDispatcher;
import com.qingyuan.secondhand.websocket.protocol.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketServer extends TextWebSocketHandler {

    private final WebSocketSessionManager sessionManager;
    private final MessageDispatcher messageDispatcher;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            log.warn("WebSocket连接建立失败：userId为空");
            session.close();
            return;
        }
        sessionManager.addSession(userId, session);
        log.info("WebSocket连接建立成功，userId={}", userId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long userId = (Long) session.getAttributes().get("userId");
        String payload = message.getPayload();
        log.debug("收到用户{}消息: {}", userId, payload);

        try {
            WebSocketMessage<?> wsMsg = WebSocketMessage.fromJson(payload);
            if (wsMsg == null || wsMsg.getType() == null) {
                log.warn("消息格式错误，userId={}", userId);
                return;
            }
            messageDispatcher.dispatch(userId, wsMsg);
        } catch (Exception e) {
            log.error("处理用户{}消息异常", userId, e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.removeSession(userId);
        }
        log.info("用户{}连接关闭, status={}", userId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long userId = (Long) session.getAttributes().get("userId");
        log.error("用户{}传输异常", userId, exception);
        if (userId != null) {
            sessionManager.removeSession(userId);
        }
    }
}
