package com.qingyuan.secondhand.config;

import com.qingyuan.secondhand.websocket.ChatHandshakeInterceptor;
import com.qingyuan.secondhand.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketServer webSocketServer;
    private final ChatHandshakeInterceptor chatHandshakeInterceptor;

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketServer, "/ws/chat")
                .addInterceptors(chatHandshakeInterceptor)
                .setAllowedOrigins(allowedOrigins.split(","));
    }
}
