package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PickupDisputeVO {
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long initiatorId;
    private Integer initiatorRole;
    private Integer disputeType;
    private String description;
    private LocalDateTime submitTime;
    private LocalDateTime responseDeadline;
    private Integer status;
    private LocalDateTime createTime;

    private String initiatorNickName;
    private String initiatorAvatar;
    private String responderNickName;
    private String responderAvatar;
}
