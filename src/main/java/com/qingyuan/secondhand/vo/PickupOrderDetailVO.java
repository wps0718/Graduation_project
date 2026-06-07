package com.qingyuan.secondhand.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PickupOrderDetailVO {
    private Long id;
    private String orderNo;
    private Long requesterId;
    private Long pickerId;

    private String pickupCode;
    private String pickupLocation;
    private String pickupDetail;
    private String deliveryLocation;

    private BigDecimal proposedPrice;
    private BigDecimal agreedPrice;
    private Long priceProposerId;
    private Integer pickerPriceConfirmed;
    private LocalDateTime priceDeadline;

    private LocalDateTime expectedDeliveryTime;
    private LocalDateTime deliveryDeadline;
    private String evidenceImages;

    private Integer requesterConfirmed;
    private Integer pickerConfirmed;

    private Integer status;
    private String cancelReason;
    private Long cancelBy;
    private LocalDateTime confirmDeadline;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String requesterNickName;
    private String requesterAvatar;
    private String requesterPhone;

    private String pickerNickName;
    private String pickerAvatar;
    private String pickerPhone;
    private BigDecimal pickerScore;
}
