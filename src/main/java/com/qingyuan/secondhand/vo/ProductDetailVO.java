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
    // 重命名：sellerNickName -> publisherNickName，与前端字段对齐
    private String publisherNickName;
    // 重命名：sellerAvatarUrl -> publisherAvatarUrl，与前端字段对齐
    private String publisherAvatarUrl;
    private BigDecimal publisherScore;
    // 重命名：sellerAuthStatus -> publisherAuthStatus，与前端字段对齐
    private Integer publisherAuthStatus;
    // 新增：驳回原因
    private String rejectReason;

    private Boolean isFavorited;
    private Boolean isOwner;
    private Boolean hasActiveOrder;
}
