package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReceivedReplyVO {
    private Long id;
    private Long productId;
    private String productTitle;
    private String productImage;
    private Long fromUserId;
    private String fromNickName;
    private String fromAvatarUrl;
    private String content;
    private LocalDateTime createTime;
    private Integer isRead;
}
