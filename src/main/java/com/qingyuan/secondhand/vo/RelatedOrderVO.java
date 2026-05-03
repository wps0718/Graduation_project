package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RelatedOrderVO {
    private Long orderId;
    private String orderNo;
    private Long buyerId;
    private String buyerNickName;
    private String buyerAvatar;
    private BigDecimal price;
    private Integer status;
    private String statusText;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private Integer cancelBy;
    private String cancelByText;
}
