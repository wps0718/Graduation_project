package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CampusDTO {
    private Long id;

    @NotBlank(message = "校区名称不能为空")
    @Size(max = 64, message = "校区名称长度不能超过64个字符")
    private String name;

    @Size(max = 255, message = "校区地址长度不能超过255个字符")
    private String address;

    @NotNull(message = "排序不能为空")
    private Integer sort;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
