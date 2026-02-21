package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportHandleDTO {
    @NotNull(message = "举报ID不能为空")
    private Long reportId;

    @NotBlank(message = "处理动作不能为空")
    @Pattern(regexp = "off_shelf|warn|ban|ignore", message = "处理动作必须为：off_shelf、warn、ban、ignore")
    private String action;

    @Size(max = 200, message = "处理结果说明不能超过200字")
    private String handleResult;
}
