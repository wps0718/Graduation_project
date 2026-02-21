package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderCreateDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;
    @NotNull(message = "校区ID不能为空")
    private Long campusId;
    @NotBlank(message = "面交地点不能为空")
    @Size(max = 100, message = "面交地点最多100字")
    private String meetingPoint;
}
