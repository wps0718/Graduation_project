package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @NotBlank(message = "昵称不能为空")
    @Size(min = 1, max = 20, message = "昵称长度为1-20位")
    private String nickName;

    @NotBlank(message = "头像不能为空")
    private String avatarUrl;

    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别不合法")
    @Max(value = 2, message = "性别不合法")
    private Integer gender;

    private Long campusId;

    @Size(max = 200, message = "个人简介不能超过200字")
    private String bio;
}
