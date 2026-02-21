package com.qingyuan.secondhand.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportStatus {
    PENDING(0, "待处理"),
    PROCESSED(1, "已处理"),
    IGNORED(2, "已忽略");

    private final Integer code;
    private final String description;

    public static ReportStatus getByCode(Integer code) {
        for (ReportStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
