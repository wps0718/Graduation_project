package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BannerVO {
    private Long id;
    private String title;
    private String image;
    private Integer linkType;
    private String linkUrl;
    private Long campusId;
    private Integer sort;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
}
