package com.qingyuan.secondhand.task;

import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketHeartbeatTask {

    private final WebSocketSessionManager sessionManager;
    private final StringRedisTemplate stringRedisTemplate;

    @Scheduled(fixedRate = 30000)
    public void checkHeartbeat() {
        Set<Long> onlineUserIds = sessionManager.getAllOnlineUserIds();
        if (onlineUserIds.isEmpty()) {
            return;
        }

        log.debug("开始检测心跳超时，当前在线{}人", onlineUserIds.size());
        int timeoutCount = 0;

        for (Long userId : onlineUserIds) {
            String heartbeat = stringRedisTemplate.opsForValue().get(RedisConstant.IM_HEARTBEAT + userId);
            if (heartbeat == null) {
                log.warn("用户{}心跳超时，强制断开", userId);
                sessionManager.removeSession(userId);
                timeoutCount++;
            }
        }

        if (timeoutCount > 0) {
            log.info("心跳检测完成，断开{}个超时连接", timeoutCount);
        }
    }
}
