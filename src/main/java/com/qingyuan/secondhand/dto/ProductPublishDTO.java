package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductPublishDTO {
    @NotBlank(message = "商品标题不能为空")
    @Size(min = 1, max = 50, message = "商品标题长度为1-50字")
    private String title;

    @NotBlank(message = "商品描述不能为空")
    @Size(min = 1, max = 500, message = "商品描述长度为1-500字")
    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @Digits(integer = 8, fraction = 2, message = "价格格式不正确")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "原价必须大于0")
    @Digits(integer = 8, fraction = 2, message = "原价格式不正确")
    private BigDecimal originalPrice;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotNull(message = "成色不能为空")
    @Min(value = 1, message = "成色值范围1-5")
    @Max(value = 5, message = "成色值范围1-5")
    private Integer conditionLevel;

    @NotNull(message = "交易校区不能为空")
    private Long campusId;

    private Long meetingPointId;

    @Size(max = 100, message = "面交地点文字不能超过100字")
    private String meetingPointText;

    @NotNull(message = "商品图片不能为空")
    @Size(min = 1, max = 9, message = "商品图片数量为1-9张")
    private List<String> images;
}
