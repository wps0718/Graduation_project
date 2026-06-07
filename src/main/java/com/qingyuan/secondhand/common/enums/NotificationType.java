package com.qingyuan.secondhand.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public enum NotificationType {
    TRADE_SUCCESS(1, "交易成功", "你购买的「{productName}」交易已完成，给卖家一个评价吧！"),
    NEW_MESSAGE(2, "新消息", "{nickName}回复了你的消息：\"{content}\""),
    AUDIT_PASS(3, "商品审核通过", "您的商品《{productName}》已通过审核，现已上架！"),
    AUDIT_REJECT(4, "商品审核驳回", "您的商品《{productName}》未通过审核，驳回原因：{reason}"),
    SYSTEM_NOTICE(5, "系统公告", "{content}"),
    BE_FAVORITED(6, "您的商品被收藏了", "你的商品《{productName}》被{count}位用户收藏了"),
    ORDER_CANCEL(7, "订单已取消", "你与{nickName}的交易「{productName}」已取消"),
    AUTH_PASS(8, "校园认证通过", "恭喜您，您的校园认证已通过审核！"),
    AUTH_REJECT(9, "校园认证被驳回", "您的校园认证未通过审核，驳回原因：{reason}"),
    REVIEW_REMIND(10, "评价提醒", "你购买的「{productName}」交易已完成3天，还未评价哦"),
    NEW_FOLLOWER(11, "新增关注", "{nickName}关注了你"),
    ORDER_SHIPPED(12, "卖家已确认发货", "卖家已确认发货，订单「{productName}」请尽快前往面交"),

    PICKUP_ACCEPTED(13, "代拿订单被接单", "有人接了您的代拿订单「{orderNo}」"),
    PICKUP_PRICE_MODIFIED(14, "代拿价格被修改", "需求者修改了报酬为 ¥{amount}，请确认"),
    PICKUP_PRICE_CONFIRMED(15, "代拿价格已确认", "价格已确认 ¥{amount}，请按约定完成代拿"),
    PICKUP_DELIVERED(16, "代拿完成待确认", "代拿者已送达，请确认（3天内未确认将自动完成）"),
    PICKUP_AUTO_CONFIRMED(17, "代拿订单自动确认完成", "订单已自动确认完成"),
    PICKUP_TIMEOUT_CANCEL(18, "代拿订单超时取消", "订单超时，已自动取消"),
    PICKUP_DELIVERY_REMIND(19, "代拿送达超时提醒", "您的代拿订单即将超时，请尽快送达"),
    PICKUP_DISPUTE_INITIATED(20, "代拿订单纠纷发起", "订单「{orderNo}」已发起纠纷，请7天内回应"),
    PICKUP_DISPUTE_RESPONDED(21, "代拿订单纠纷已响应", "对方已回应，等待平台裁决"),
    PICKUP_DISPUTE_3DAY(22, "纠纷3天提醒", "纠纷回应还剩4天，请尽快提供证据"),
    PICKUP_DISPUTE_6DAY(23, "纠纷6天提醒", "纠纷回应还剩1天，逾期将自动判定对方胜诉"),
    PICKUP_DISPUTE_AUTO_WIN(24, "纠纷超时自动裁决", "对方未回应，申诉方自动胜诉"),
    PICKUP_DISPUTE_JUDGED(25, "纠纷管理员裁决完成", "纠纷已裁决：{result}");

    private final Integer code;
    private final String description;
    private final String template;

    public static NotificationType getByCode(Integer code) {
        for (NotificationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public String formatContent(Map<String, String> params) {
        String content = template;
        if (content == null || params == null || params.isEmpty()) {
            return content;
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key != null) {
                content = content.replace("{" + key + "}", value == null ? "" : value);
            }
        }
        return content;
    }
}
