package com.qingyuan.secondhand.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING_MEET(1, "待面交"),
    COMPLETED(3, "已完成"),
    RATED(4, "已评价"),
    CANCELLED(5, "已取消");

    private final Integer code;
    private final String description;

    public static OrderStatus getByCode(Integer code) {
        for (OrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
