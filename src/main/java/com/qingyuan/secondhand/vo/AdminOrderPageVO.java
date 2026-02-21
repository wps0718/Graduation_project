package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminOrderPageVO {
    private Long id;
    private String orderNo;
    private BigDecimal price;
    private Integer status;
    private LocalDateTime createTime;
    private Long productId;
    private String productTitle;
    private String productCoverImage;
    private String campusName;
    private Long buyerId;
    private String buyerNickName;
    private String buyerPhone;
    private Long sellerId;
    private String sellerNickName;
    private String sellerPhone;
}
