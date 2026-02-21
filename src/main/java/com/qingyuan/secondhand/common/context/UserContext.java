package com.qingyuan.secondhand.common.context;

public class UserContext {

    private static final ThreadLocal<Long> userThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> userTypeThreadLocal = new ThreadLocal<>();

    public static void setCurrentUserId(Long userId) {
        userThreadLocal.set(userId);
    }

    public static Long getCurrentUserId() {
        return userThreadLocal.get();
    }

    public static void setCurrentUserType(String userType) {
        userTypeThreadLocal.set(userType);
    }

    public static String getCurrentUserType() {
        return userTypeThreadLocal.get();
    }

    public static void removeCurrentUserId() {
        userThreadLocal.remove();
    }

    public static void removeCurrentUserType() {
        userTypeThreadLocal.remove();
    }
}
