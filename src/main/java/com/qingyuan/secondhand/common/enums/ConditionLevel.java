package com.qingyuan.secondhand.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConditionLevel {
    BRAND_NEW(1, "全新"),
    ALMOST_NEW(2, "几乎全新"),
    NINETY_PERCENT(3, "九成新"),
    EIGHTY_PERCENT(4, "八成新"),
    SEVENTY_PERCENT_OR_LESS(5, "七成新及以下");

    private final Integer code;
    private final String description;

    public static ConditionLevel getByCode(Integer code) {
        for (ConditionLevel level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        return null;
    }
}
