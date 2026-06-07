package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PickupDisputeSubmitDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "纠纷类型不能为空")
    private Integer disputeType;

    @NotBlank(message = "申诉描述不能为空")
    @Size(max = 500, message = "申诉描述不能超过500字")
    private String description;

    @NotEmpty(message = "请至少提供一项证据")
    @Size(max = 9, message = "证据最多9项")
    private List<EvidenceItem> evidenceMaterials;

    @Data
    public static class EvidenceItem {
        @NotNull(message = "证据类型不能为空")
        private String type;
        private String url;
        private String content;
        private String description;
    }
}
