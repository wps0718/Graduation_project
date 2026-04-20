package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminProductPageVO {
    private Long id;
    private String title;
    private BigDecimal price;
    private String coverImage;
    private Integer conditionLevel;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer status;
    private String categoryName;
    private String campusName;
    private LocalDateTime createTime;

    private Long sellerId;
    // 重命名：sellerNickName -> publisherNickName，与前端字段对齐
    private String publisherNickName;
    // 重命名：sellerAvatarUrl -> publisherAvatarUrl，与前端字段对齐
    private String publisherAvatarUrl;
    // 新增：发布者认证状态
    private Integer publisherAuthStatus;
    // 新增：商品原价
    private BigDecimal originalPrice;
}
