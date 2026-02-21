package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthPageVO {
    private Long id;
    private Long userId;
    private String nickName;
    private String avatarUrl;
    private Long collegeId;
    private String collegeName;
    private String studentNo;
    private String className;
    private String certImage;
    private Integer status;
    private String rejectReason;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
}
