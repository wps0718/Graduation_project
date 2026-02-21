package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoriteListVO {
    private Long favoriteId;
    private Long productId;
    private LocalDateTime favoriteTime;
    private String title;
    private BigDecimal price;
    private String coverImage;
    private Integer status;
    private Integer conditionLevel;
    private String campusName;
    private Long sellerId;
    private String sellerNickName;
}
