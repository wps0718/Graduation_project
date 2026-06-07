package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DisputeHandleDTO {

    @NotNull(message = "纠纷ID不能为空")
    private Long disputeId;

    @NotNull(message = "裁决结果不能为空")
    private Integer judgmentResult;

    @NotBlank(message = "裁决说明不能为空")
    @Size(max = 500, message = "裁决说明不能超过500字")
    private String judgmentDetail;

    private Long penaltyUserId;

    private BigDecimal penaltyScore;
}
