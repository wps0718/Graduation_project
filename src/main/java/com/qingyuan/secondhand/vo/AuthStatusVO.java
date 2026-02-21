package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthStatusVO {
    private Integer status;
    private String collegeName;
    private String studentNo;
    private String className;
    private String certImage;
    private String rejectReason;
    private LocalDateTime reviewTime;
}
