package com.qingyuan.secondhand.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationCategory {
    TRANSACTION(1, "交易"),
    SYSTEM(2, "系统");

    private final Integer code;
    private final String description;

    public static NotificationCategory getByCode(Integer code) {
        for (NotificationCategory category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        return null;
    }
}
