package com.qingyuan.secondhand.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BannerLinkType {
    PRODUCT_DETAIL(1, "商品详情"),
    ACTIVITY_PAGE(2, "活动页"),
    EXTERNAL_LINK(3, "外部链接");

    private final Integer code;
    private final String description;

    public static BannerLinkType getByCode(Integer code) {
        for (BannerLinkType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
