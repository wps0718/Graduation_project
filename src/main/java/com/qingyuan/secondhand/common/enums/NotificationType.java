package com.qingyuan.secondhand.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {
    TYPE_1(1, "Type 1"),
    TYPE_2(2, "Type 2"),
    TYPE_3(3, "Type 3"),
    TYPE_4(4, "Type 4"),
    TYPE_5(5, "Type 5"),
    TYPE_6(6, "Type 6"),
    TYPE_7(7, "Type 7"),
    TYPE_8(8, "Type 8"),
    TYPE_9(9, "Type 9"),
    TYPE_10(10, "Type 10");

    private final Integer code;
    private final String description;

    public static NotificationType getByCode(Integer code) {
        for (NotificationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
