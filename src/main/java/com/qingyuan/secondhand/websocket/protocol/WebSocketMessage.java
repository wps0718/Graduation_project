package com.qingyuan.secondhand.websocket.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
public class WebSocketMessage<T> {
    private String type;
    private T data;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static String toJson(WebSocketMessage<?> message) {
        try {
            return OBJECT_MAPPER.writeValueAsString(message);
        } catch (Exception e) {
            log.error("WebSocketMessage序列化失败", e);
            return null;
        }
    }

    public static WebSocketMessage<Object> fromJson(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, WebSocketMessage.class);
        } catch (Exception e) {
            log.error("WebSocketMessage反序列化失败: {}", json, e);
            throw new RuntimeException("消息格式错误");
        }
    }
}
