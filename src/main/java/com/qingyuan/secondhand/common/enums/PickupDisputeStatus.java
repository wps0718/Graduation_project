package com.qingyuan.secondhand.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PickupDisputeStatus {
    PENDING_RESPONSE(0, "待响应"),
    RESPONDED(1, "已响应待裁决"),
    AUTO_WIN(2, "申诉方胜诉"),
    JUDGED(3, "已裁决"),
    WITHDRAWN(4, "已撤销");

    private final Integer code;
    private final String description;

    public static PickupDisputeStatus getByCode(Integer code) {
        for (PickupDisputeStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
