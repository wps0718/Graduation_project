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
    NEW_FOLLOWER(11, "新增关注", "{nickName}关注了你");

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
