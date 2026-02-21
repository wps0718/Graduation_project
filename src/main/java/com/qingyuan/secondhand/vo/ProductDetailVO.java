package com.qingyuan.secondhand.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDetailVO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer conditionLevel;
    private List<String> images;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer status;
    private LocalDateTime createTime;
    @JsonIgnore
    private String imagesJson;

    private String categoryName;
    private String campusName;
    private String meetingPointName;
    private String meetingPointText;

    private Long sellerId;
    private String sellerNickName;
    private String sellerAvatarUrl;
    private BigDecimal sellerScore;
    private Integer sellerAuthStatus;

    private Boolean isFavorited;
    private Boolean isOwner;
    private Boolean hasActiveOrder;
}
