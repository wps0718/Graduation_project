package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeVO {
    private Long id;
    private String username;
    private String name;
    private String phone;
    private Integer role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
