package com.qingyuan.secondhand.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class OrderNoUtil {
    private static final String PREFIX = "TD";
    private static final Random RANDOM = new Random();

    public static String generate() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomNum = String.format("%04d", RANDOM.nextInt(10000));
        return PREFIX + timestamp + randomNum;
    }
}
