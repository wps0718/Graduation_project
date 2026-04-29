package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductListVO {
    private Long id;
    private String title;
    private BigDecimal price;
    private String coverImage;
    private Integer conditionLevel;
    private Integer viewCount;
    private Integer favoriteCount;
    private String campusName;
    private LocalDateTime createTime;

    private Long sellerId;
    private String sellerNickName;
    private String sellerAvatarUrl;

    // 商品状态：0待审核/1在售/2已下架/3已售出/4审核驳回
    private Integer status;

    private Integer chatCount; // 询问次数（聊天会话数）
}