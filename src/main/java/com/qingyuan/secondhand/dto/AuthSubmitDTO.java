package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthSubmitDTO {

    @NotNull(message = "学院ID不能为空")
    private Long collegeId;

    @NotBlank(message = "学号不能为空")
    @Size(max = 32, message = "学号长度不能超过32个字符")
    private String studentNo;

    @NotBlank(message = "班级不能为空")
    @Size(max = 64, message = "班级长度不能超过64个字符")
    private String className;

    @NotBlank(message = "认证材料图片不能为空")
    private String certImage;
}
