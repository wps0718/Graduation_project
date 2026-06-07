package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PickupDisputeDetailVO {
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
    private Integer reminderCount;

    private Integer status;
    private Long adminId;
    private Integer judgmentResult;
    private String judgmentDetail;
    private Long penaltyUserId;
    private BigDecimal penaltyScore;
    private LocalDateTime resolveTime;
    private LocalDateTime createTime;

    private String initiatorNickName;
    private String initiatorAvatar;
    private String responderNickName;
    private String responderAvatar;

    private String requesterNickName;
    private String pickerNickName;

    private List<EvidenceVO> initiatorEvidence;
    private List<EvidenceVO> responderEvidence;

    @Data
    public static class EvidenceVO {
        private Long id;
        private Integer type;
        private String url;
        private String content;
        private String description;
        private Integer sortOrder;
    }
}
