package com.qingyuan.secondhand.common.util;

import org.springframework.util.StringUtils;

public class PhoneUtil {
    public static String maskPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return "";
        }
        if (phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
