package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PickupPriceModifyDTO {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "报酬金额不能为空")
    @DecimalMin(value = "0.01", message = "报酬金额必须大于0")
    private BigDecimal proposedPrice;

    private LocalDateTime expectedDeliveryTime;
}
