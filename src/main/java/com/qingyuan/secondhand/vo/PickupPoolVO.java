package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PickupPoolVO {
    private Long id;
    private String orderNo;
    private String pickupLocation;
    private String pickupDetail;
    private String deliveryLocation;
    private BigDecimal proposedPrice;
    private LocalDateTime expectedDeliveryTime;
    private LocalDateTime createTime;
    private String campusName;
    private Long requesterId;
    private String requesterNickName;
    private String requesterAvatar;
}
