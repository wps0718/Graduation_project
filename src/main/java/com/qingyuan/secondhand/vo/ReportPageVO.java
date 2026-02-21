package com.qingyuan.secondhand.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportPageVO {
    private Long id;
    private Integer targetType;
    private Integer reasonType;
    private String description;
    private Integer status;
    private LocalDateTime createTime;

    private Long reporterId;
    private String reporterNickName;
    private String reporterAvatarUrl;

    private Long targetId;

    private String productTitle;
    private String productCoverImage;
    @JsonIgnore
    private String productCoverImageJson;
    private Long productUserId;
    private String productUserNickName;

    private String targetUserNickName;
    private String targetUserAvatarUrl;
}
