package com.qingyuan.secondhand.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportReason {
    FAKE_ITEM(1, "虚假商品"),
    PROHIBITED_ITEM(2, "违禁物品"),
    SCAM(3, "诈骗信息"),
    INAPPROPRIATE_CONTENT(4, "不当内容"),
    OTHER(5, "其他");

    private final Integer code;
    private final String description;

    public static ReportReason getByCode(Integer code) {
        for (ReportReason reason : values()) {
            if (reason.getCode().equals(code)) {
                return reason;
            }
        }
        return null;
    }
}
