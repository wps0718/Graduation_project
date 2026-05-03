package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PublisherInfoVO {
    private Long userId;
    private String avatarUrl;
    private String nickName;
    private String phone;
    private Integer accountStatus;
    private String accountStatusText;
    private Integer authStatus;
    private String authStatusText;
    private BigDecimal score;
    private String bio;
    private String ipRegion;
    private LocalDateTime createTime;
    private Integer productCount;
    private Integer dealOrderCount;
    private String realName;
    private String collegeName;
    private String studentNo;
}
