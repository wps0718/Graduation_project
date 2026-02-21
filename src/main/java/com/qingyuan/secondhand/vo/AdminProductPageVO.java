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
    private String sellerNickName;
    private String sellerAvatarUrl;
}
