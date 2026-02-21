package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BannerDTO {

    @NotBlank(message = "Banner标题不能为空")
    @Size(max = 64, message = "Banner标题不能超过64字")
    private String title;

    @NotBlank(message = "Banner图片不能为空")
    @Size(max = 255, message = "图片URL不能超过255字")
    private String image;

    private Integer linkType;

    @Size(max = 255, message = "跳转地址不能超过255字")
    private String linkUrl;

    private Long campusId;

    @NotNull(message = "排序号不能为空")
    private Integer sort;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
