package com.qingyuan.secondhand.websocket;

import com.qingyuan.secondhand.common.util.JwtUtil;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            URI uri = request.getURI();
            String query = uri.getQuery();
            if (query == null || !query.contains("token=")) {
                log.warn("WebSocket握手失败：缺少token参数");
                return false;
            }

            String token = extractToken(query);
            if (token == null || token.isEmpty()) {
                log.warn("WebSocket握手失败：token为空");
                return false;
            }

            Claims claims = jwtUtil.parseToken(token);
            String type = (String) claims.get("type");
            if (!"mini".equals(type)) {
                log.warn("WebSocket握手失败：token类型不是mini，type={}", type);
                return false;
            }

            Long userId = Long.parseLong(claims.getSubject());
            User user = userMapper.selectById(userId);
            if (user == null) {
                log.warn("WebSocket握手失败：用户不存在，userId={}", userId);
                return false;
            }

            Integer status = user.getStatus();
            if (status != null && (status == 0 || status == 2)) {
                log.warn("WebSocket握手失败：用户状态异常，userId={}, status={}", userId, status);
                return false;
            }

            attributes.put("userId", userId);
            log.info("WebSocket握手成功，userId={}", userId);
            return true;
        } catch (Exception e) {
            log.error("WebSocket握手异常", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    private String extractToken(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                return param.substring(6);
            }
        }
        return null;
    }
}
