package com.qingyuan.secondhand.websocket;

import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.websocket.protocol.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketSessionManager {

    private final StringRedisTemplate stringRedisTemplate;
    private final ConcurrentHashMap<Long, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();

    public void addSession(Long userId, WebSocketSession session) {
        WebSocketSession oldSession = onlineSessions.get(userId);
        if (oldSession != null && oldSession.isOpen()) {
            try {
                Map<String, String> data = Map.of("reason", "账号在其他设备登录");
                WebSocketMessage<Map<String, String>> msg = new WebSocketMessage<>("FORCE_OFFLINE", data);
                oldSession.sendMessage(new TextMessage(WebSocketMessage.toJson(msg)));
                oldSession.close();
                log.info("用户{}旧连接已踢下线", userId);
            } catch (IOException e) {
                log.error("关闭用户{}旧连接失败", userId, e);
            }
        }

        onlineSessions.put(userId, session);
        stringRedisTemplate.opsForValue().set(RedisConstant.IM_ONLINE + userId, "1");
        log.info("用户{}上线，当前在线{}人", userId, onlineSessions.size());
    }

    public void removeSession(Long userId) {
        WebSocketSession session = onlineSessions.remove(userId);
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                log.error("关闭用户{}连接失败", userId, e);
            }
        }
        stringRedisTemplate.delete(RedisConstant.IM_ONLINE + userId);
        log.info("用户{}下线，当前在线{}人", userId, onlineSessions.size());
    }

    public WebSocketSession getSession(Long userId) {
        return onlineSessions.get(userId);
    }

    public boolean isOnline(Long userId) {
        WebSocketSession session = onlineSessions.get(userId);
        return session != null && session.isOpen();
    }

    public boolean sendToUser(Long userId, WebSocketMessage<?> message) {
        WebSocketSession session = onlineSessions.get(userId);
        if (session == null || !session.isOpen()) {
            return false;
        }

        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(WebSocketMessage.toJson(message)));
            }
            return true;
        } catch (IOException e) {
            log.error("发送消息给用户{}失败", userId, e);
            removeSession(userId);
            return false;
        }
    }

    public int getOnlineCount() {
        return onlineSessions.size();
    }

    public Set<Long> getAllOnlineUserIds() {
        return onlineSessions.keySet();
    }
}
