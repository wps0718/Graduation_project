package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PickupEvidenceDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotEmpty(message = "请至少上传一张证据照片")
    @Size(max = 9, message = "证据照片最多9张")
    private List<String> evidenceImages;
}
