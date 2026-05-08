package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderCreateDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @DecimalMin(value = "0.01", message = "成交价格必须大于0")
    private BigDecimal price;

    private Long meetingPointId;

    private String meetingPointText;

    private String remark;
}
