package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NoticeDTO {
    private Long id;

    @NotBlank(message = "公告标题不能为空")
    @Size(max = 64, message = "公告标题最多64个字符")
    private String title;

    @NotBlank(message = "公告内容不能为空")
    @Size(max = 500, message = "公告内容最多500个字符")
    private String content;

    @NotNull(message = "公告类型不能为空")
    private Integer type;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
