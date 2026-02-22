package com.qingyuan.secondhand.websocket;

import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.websocket.protocol.WebSocketMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketSessionManagerTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private WebSocketSessionManager sessionManager;

    @Test
    void testAddSession_正常添加() throws IOException {
        Long userId = 10001L;
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        sessionManager.addSession(userId, session);

        assertEquals(1, sessionManager.getOnlineCount());
        assertTrue(sessionManager.isOnline(userId));
        verify(valueOperations).set(RedisConstant.IM_ONLINE + userId, "1");
    }

    @Test
    void testAddSession_踢掉旧连接() throws IOException {
        Long userId = 10001L;
        WebSocketSession oldSession = mock(WebSocketSession.class);
        WebSocketSession newSession = mock(WebSocketSession.class);
        when(oldSession.isOpen()).thenReturn(true);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        sessionManager.addSession(userId, oldSession);
        sessionManager.addSession(userId, newSession);

        assertEquals(1, sessionManager.getOnlineCount());
        verify(oldSession).sendMessage(any(TextMessage.class));
        verify(oldSession).close();
    }

    @Test
    void testRemoveSession_正常移除() {
        Long userId = 10001L;
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        sessionManager.addSession(userId, session);

        sessionManager.removeSession(userId);

        assertEquals(0, sessionManager.getOnlineCount());
        assertFalse(sessionManager.isOnline(userId));
        verify(stringRedisTemplate).delete(RedisConstant.IM_ONLINE + userId);
    }

    @Test
    void testIsOnline_在线判断() {
        Long userId = 10001L;
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        sessionManager.addSession(userId, session);

        assertTrue(sessionManager.isOnline(userId));
        assertFalse(sessionManager.isOnline(10002L));
    }

    @Test
    void testSendToUser_在线发送成功() throws IOException {
        Long userId = 10001L;
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        sessionManager.addSession(userId, session);

        WebSocketMessage<String> message = new WebSocketMessage<>("PONG", null);

        boolean result = sessionManager.sendToUser(userId, message);

        assertTrue(result);
        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    void testSendToUser_离线返回false() {
        Long userId = 10001L;
        WebSocketMessage<String> message = new WebSocketMessage<>("PONG", null);

        boolean result = sessionManager.sendToUser(userId, message);

        assertFalse(result);
    }

    @Test
    void testGetAllOnlineUserIds() {
        WebSocketSession session1 = mock(WebSocketSession.class);
        WebSocketSession session2 = mock(WebSocketSession.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        sessionManager.addSession(10001L, session1);
        sessionManager.addSession(10002L, session2);

        Set<Long> onlineUserIds = sessionManager.getAllOnlineUserIds();

        assertEquals(2, onlineUserIds.size());
        assertTrue(onlineUserIds.contains(10001L));
        assertTrue(onlineUserIds.contains(10002L));
    }
}
