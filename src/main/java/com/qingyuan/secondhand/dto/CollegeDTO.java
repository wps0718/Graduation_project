package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CollegeDTO {
    private Long id;

    @NotBlank(message = "学院名称不能为空")
    @Size(max = 50, message = "学院名称不能超过50字")
    private String name;

    @NotNull(message = "排序不能为空")
    private Integer sort;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
