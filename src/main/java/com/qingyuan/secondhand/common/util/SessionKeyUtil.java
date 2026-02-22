package com.qingyuan.secondhand.common.util;

public class SessionKeyUtil {
    public static String buildSessionKey(Long userA, Long userB, Long productId) {
        long min = Math.min(userA, userB);
        long max = Math.max(userA, userB);
        return min + "_" + max + "_" + (productId != null ? productId : 0);
    }

    public static boolean isParticipant(String sessionKey, Long userId) {
        if (sessionKey == null || userId == null) {
            return false;
        }
        String[] parts = sessionKey.split("_");
        if (parts.length < 2) {
            return false;
        }
        try {
            long userA = Long.parseLong(parts[0]);
            long userB = Long.parseLong(parts[1]);
            return userId.equals(userA) || userId.equals(userB);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static Long getPeerId(String sessionKey, Long userId) {
        if (!isParticipant(sessionKey, userId)) {
            return null;
        }
        String[] parts = sessionKey.split("_");
        long userA = Long.parseLong(parts[0]);
        long userB = Long.parseLong(parts[1]);
        return userId.equals(userA) ? userB : userA;
    }

    public static Long getProductId(String sessionKey) {
        if (sessionKey == null) {
            return null;
        }
        String[] parts = sessionKey.split("_");
        if (parts.length < 3) {
            return null;
        }
        long productId = Long.parseLong(parts[2]);
        return productId == 0 ? null : productId;
    }
}
