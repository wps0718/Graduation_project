package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteDTO {
    @NotNull(message = "商品ID不能为空")
    private Long productId;
}
