package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeDTO {
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(max = 32, message = "用户名最多32个字符")
    private String username;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 32, message = "姓名最多32个字符")
    private String name;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotNull(message = "角色不能为空")
    private Integer role;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
