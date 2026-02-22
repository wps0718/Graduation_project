package com.qingyuan.secondhand.websocket.protocol;

import lombok.Getter;

@Getter
public enum MessageType {
    CHAT("chat"),
    READ("read"),
    READ_ACK("read_ack"),
    PING("ping"),
    PONG("pong"),
    SYSTEM("system"),
    FORCE_OFFLINE("force_offline");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    public static MessageType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (MessageType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}
