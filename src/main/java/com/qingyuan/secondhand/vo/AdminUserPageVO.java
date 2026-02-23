package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserPageVO {
    private Long id;
    private String nickName;
    private String phone;
    private String avatarUrl;
    private Integer authStatus;
    private Integer status;
    private String banReason;
    private Long campusId;
    private String campusName;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;
}
