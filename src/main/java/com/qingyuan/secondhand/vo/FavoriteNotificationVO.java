package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoriteNotificationVO {
    private Long id;
    private Long userId;
    private String nickName;
    private String avatarUrl;
    private Long productId;
    private String productTitle;
    private String productImage;
    private BigDecimal productPrice;
    private Integer isRead;
    private LocalDateTime createTime;
}
