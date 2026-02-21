package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoticeVO {
    private Long id;
    private String title;
    private String content;
    private Integer type;
    private Integer status;
    private Long publisherId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
