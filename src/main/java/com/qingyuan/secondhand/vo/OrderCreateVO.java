package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderCreateVO {
    private Long orderId;
    private String orderNo;
    private LocalDateTime expireTime;
}
