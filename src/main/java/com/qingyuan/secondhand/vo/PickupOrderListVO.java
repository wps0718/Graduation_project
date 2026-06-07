package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PickupOrderListVO {
    private Long id;
    private String orderNo;
    private String pickupLocation;
    private String deliveryLocation;
    private BigDecimal proposedPrice;
    private BigDecimal agreedPrice;
    private Integer status;
    private LocalDateTime expectedDeliveryTime;
    private LocalDateTime createTime;

    private Long requesterId;
    private String requesterNickName;
    private String requesterAvatar;

    private Long pickerId;
    private String pickerNickName;
    private String pickerAvatar;
}
