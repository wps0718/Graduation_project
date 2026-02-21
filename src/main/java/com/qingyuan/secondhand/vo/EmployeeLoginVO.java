package com.qingyuan.secondhand.vo;

import lombok.Data;

@Data
public class EmployeeLoginVO {
    private Long id;
    private String username;
    private String name;
    private Integer role;
    private String token;
}
