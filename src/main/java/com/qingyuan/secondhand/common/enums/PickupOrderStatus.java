package com.qingyuan.secondhand.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PickupOrderStatus {
    PENDING_ACCEPT(0, "待接单"),
    ACCEPTED(1, "已接单"),
    PRICE_CONFIRMED(2, "价格已确认"),
    PICKING_UP(3, "代拿中"),
    DELIVERED(4, "已代拿"),
    COMPLETED(5, "已完成"),
    RATED(6, "已评价"),
    CANCELLED(7, "已取消"),
    DISPUTE(8, "纠纷中");

    private final Integer code;
    private final String description;

    public static PickupOrderStatus getByCode(Integer code) {
        for (PickupOrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
