package com.qingyuan.secondhand.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pickup_order")
public class PickupOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long requesterId;
    private Long pickerId;
    private Long campusId;

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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer paymentStatus;
    private String paymentOrderId;

    private Integer isDeleted;
}
