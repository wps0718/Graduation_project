package com.qingyuan.secondhand.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthStatus {
    UNAUTHENTICATED(0, "未认证"),
    PENDING(1, "审核中"),
    AUTHENTICATED(2, "已认证"),
    REJECTED(3, "已驳回");

    private final Integer code;
    private final String description;

    public static AuthStatus getByCode(Integer code) {
        for (AuthStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
