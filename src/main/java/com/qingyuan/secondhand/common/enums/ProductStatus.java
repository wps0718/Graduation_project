package com.qingyuan.secondhand.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductStatus {
    PENDING(0, "待审核"),
    ON_SALE(1, "在售"),
    OFF_SHELF(2, "已下架"),
    SOLD(3, "已售出"),
    REJECTED(4, "审核驳回");

    private final Integer code;
    private final String description;

    public static ProductStatus getByCode(Integer code) {
        for (ProductStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
