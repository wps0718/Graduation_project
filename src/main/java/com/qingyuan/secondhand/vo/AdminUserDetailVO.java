package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminUserDetailVO {
    private Long id;
    private String nickName;
    private String username;
    private String phone;
    private String avatarUrl;
    private Integer gender;
    private Long campusId;
    private String campusName;
    private Integer authStatus;
    private Integer status;
    private String banReason;
    private BigDecimal score;
    private LocalDateTime deactivateTime;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
