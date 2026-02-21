package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewSubmitDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "描述评分不能为空")
    @Min(value = 1, message = "描述评分范围1-5")
    @Max(value = 5, message = "描述评分范围1-5")
    private Integer scoreDesc;

    @NotNull(message = "态度评分不能为空")
    @Min(value = 1, message = "态度评分范围1-5")
    @Max(value = 5, message = "态度评分范围1-5")
    private Integer scoreAttitude;

    @NotNull(message = "体验评分不能为空")
    @Min(value = 1, message = "体验评分范围1-5")
    @Max(value = 5, message = "体验评分范围1-5")
    private Integer scoreExperience;

    @Size(max = 200, message = "评价内容不能超过200字")
    private String content;
}
