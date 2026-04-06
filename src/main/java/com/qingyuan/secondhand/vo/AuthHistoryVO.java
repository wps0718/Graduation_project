package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthHistoryVO {
    private Long id;
    private Long authId;
    private Long collegeId;
    private String collegeName;
    private String realName;
    private String studentNo;
    private String className;
    private String certImage;
    private Integer status;
    private String rejectReason;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
}

