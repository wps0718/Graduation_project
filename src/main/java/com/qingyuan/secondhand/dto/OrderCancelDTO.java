package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrderCancelDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    @Size(max = 200, message = "取消原因最多200字")
    private String cancelReason;
}
