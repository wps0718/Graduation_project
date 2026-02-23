package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRejectDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotBlank(message = "驳回原因不能为空")
    private String rejectReason;
}
