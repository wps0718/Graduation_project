package com.qingyuan.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pickup_dispute")
public class PickupDispute {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderNo;

    private Long initiatorId;
    private Integer initiatorRole;
    private Integer disputeType;
    private String description;
    private LocalDateTime submitTime;

    private Long responderId;
    private String responseDescription;
    private LocalDateTime responseTime;
    private LocalDateTime responseDeadline;
    private LocalDateTime lastReminderTime;
    private Integer reminderCount;

    private Integer status;
    private Long adminId;
    private Integer judgmentResult;
    private String judgmentDetail;
    private Long penaltyUserId;
    private BigDecimal penaltyScore;
    private LocalDateTime resolveTime;

    private LocalDateTime createTime;
}
