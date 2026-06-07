package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PickupDisputeRespondDTO {

    @NotNull(message = "纠纷ID不能为空")
    private Long disputeId;

    @NotBlank(message = "回应描述不能为空")
    @Size(max = 500, message = "回应描述不能超过500字")
    private String responseDescription;

    @NotEmpty(message = "请至少提供一项证据")
    @Size(max = 9, message = "证据最多9项")
    private List<PickupDisputeSubmitDTO.EvidenceItem> responseMaterials;
}
