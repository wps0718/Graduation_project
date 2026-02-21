package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderListVO {
    private Long id;
    private String orderNo;
    private BigDecimal price;
    private Integer status;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
    private Long productId;
    private String productTitle;
    private String productCoverImage;
    private Long otherUserId;
    private String otherUserNickName;
    private String otherUserAvatar;
}
