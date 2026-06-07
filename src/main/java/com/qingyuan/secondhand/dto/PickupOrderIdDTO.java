package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PickupOrderIdDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;
}
