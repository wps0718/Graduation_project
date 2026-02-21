package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProductUpdateDTO extends ProductPublishDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
}
