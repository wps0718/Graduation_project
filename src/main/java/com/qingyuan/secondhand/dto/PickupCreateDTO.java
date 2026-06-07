package com.qingyuan.secondhand.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PickupCreateDTO {

    @NotBlank(message = "取件码不能为空")
    private String pickupCode;

    @NotBlank(message = "取件地点不能为空")
    private String pickupLocation;

    private String pickupDetail;

    @NotBlank(message = "送达地点不能为空")
    private String deliveryLocation;

    @NotNull(message = "期望送达时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectedDeliveryTime;

    @NotNull(message = "报酬金额不能为空")
    @DecimalMin(value = "0.01", message = "报酬金额必须大于0")
    private BigDecimal proposedPrice;
}
