package com.qingyuan.secondhand.websocket.handler;

import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.websocket.WebSocketSessionManager;
import com.qingyuan.secondhand.websocket.protocol.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class PingMessageHandler {

    private final WebSocketSessionManager sessionManager;
    private final StringRedisTemplate stringRedisTemplate;

    public void handle(Long userId) {
        stringRedisTemplate.opsForValue().set(
                RedisConstant.IM_HEARTBEAT + userId,
                String.valueOf(System.currentTimeMillis()),
                60,
                TimeUnit.SECONDS
        );

        WebSocketMessage<Void> pong = new WebSocketMessage<>("PONG", null);
        sessionManager.sendToUser(userId, pong);

        log.debug("用户{}心跳PING，已回复PONG", userId);
    }
}
