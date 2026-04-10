package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductCommentVO {
    private Long id;
    private Long productId;
    private Long userId;
    private String nickName;
    private String avatarUrl;
    private Long parentId;
    private Long rootId;
    private Long replyToUserId;
    private String replyToNickName;
    private String content;
    private LocalDateTime createTime;
    private List<ProductCommentVO> replies;
}
