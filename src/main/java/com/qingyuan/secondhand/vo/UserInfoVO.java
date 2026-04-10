package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserInfoVO {
    private Long id;
    private String nickName;
    private String avatarUrl;
    private String phone;
    private Integer gender;
    private Long campusId;
    private String campusName;
    private Integer authStatus;
    private BigDecimal score;
    private Integer status;
    private String bio;
}
