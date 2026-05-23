package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FootprintItemVO {
    private Long id;
    private Long productId;
    private String title;
    private String coverImage;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String campusName;
    private String sellerName;
    private Integer status;
    private String statusText;
    private LocalDateTime browseTime;
    private Long categoryId;
    private String categoryName;
}
