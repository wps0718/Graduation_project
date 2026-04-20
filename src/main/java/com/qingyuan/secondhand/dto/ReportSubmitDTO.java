package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ReportSubmitDTO {
    @NotNull(message = "被举报目标ID不能为空")
    private Long targetId;

    @NotNull(message = "目标类型不能为空")
    @Min(value = 1, message = "目标类型必须为1或2")
    @Max(value = 2, message = "目标类型必须为1或2")
    private Integer targetType;

    @NotNull(message = "举报原因类型不能为空")
    @Min(value = 1, message = "举报原因类型范围1-5")
    @Max(value = 5, message = "举报原因类型范围1-5")
    private Integer reasonType;

    @Size(max = 500, message = "举报描述不能超过500字")
    private String description;
}
