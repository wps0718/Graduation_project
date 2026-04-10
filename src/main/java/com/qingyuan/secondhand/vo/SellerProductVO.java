package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SellerProductVO {
    private Long id;
    private String title;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String coverImage;
    private Integer conditionLevel;
    private String campusName;
    private LocalDateTime createTime;
}

