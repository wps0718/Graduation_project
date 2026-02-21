package com.qingyuan.secondhand.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailVO {
    private Long id;
    private String orderNo;
    private BigDecimal price;
    private String campusName;
    private String meetingPoint;
    private Integer status;
    private String cancelReason;
    private Long cancelBy;
    private LocalDateTime expireTime;
    private LocalDateTime confirmDeadline;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private Long productId;
    private String productTitle;
    private String productDescription;
    private List<String> productImages;
    @JsonIgnore
    private String productImagesJson;
    private Integer productStatus;
    private Long buyerId;
    private String buyerNickName;
    private String buyerAvatar;
    private String buyerPhone;
    private Long sellerId;
    private String sellerNickName;
    private String sellerAvatar;
    private String sellerPhone;
    private String currentRole;
}
