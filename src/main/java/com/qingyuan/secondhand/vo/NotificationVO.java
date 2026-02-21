package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private Long id;
    private Integer type;
    private String title;
    private String content;
    private Long relatedId;
    private Integer relatedType;
    private Integer isRead;
    private Integer category;
    private LocalDateTime createTime;
}
