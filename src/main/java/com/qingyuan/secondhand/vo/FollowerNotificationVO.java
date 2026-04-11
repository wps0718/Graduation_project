package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowerNotificationVO {
    private Long id;
    private Long userId;
    private String nickName;
    private String avatarUrl;
    private Integer isRead;
    private LocalDateTime createTime;
    private Boolean isFollowing; // 当前登录用户是否也关注了该用户（回关状态）
}
