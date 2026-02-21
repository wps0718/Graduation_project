package com.qingyuan.secondhand.common.context;

public class UserContext {

    private static final ThreadLocal<Long> userThreadLocal = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        userThreadLocal.set(userId);
    }

    public static Long getCurrentUserId() {
        return userThreadLocal.get();
    }

    public static void removeCurrentUserId() {
        userThreadLocal.remove();
    }
}
