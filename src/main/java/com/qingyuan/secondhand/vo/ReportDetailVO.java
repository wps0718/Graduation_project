package com.qingyuan.secondhand.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportDetailVO {
    private Long id;
    private Integer targetType;
    private Integer reasonType;
    private String description;
    private Integer status;
    private String handleResult;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;

    private Long reporterId;
    private String reporterNickName;
    private String reporterAvatarUrl;
    private String reporterPhone;

    private Long targetId;

    private String productTitle;
    private String productDescription;
    private BigDecimal productPrice;
    private List<String> productImages;
    @JsonIgnore
    private String productImagesJson;
    private Integer productStatus;
    private Long productUserId;
    private String productUserNickName;
    private String productUserPhone;

    private String targetUserNickName;
    private String targetUserAvatarUrl;
    private String targetUserPhone;
    private Integer targetUserStatus;
    private Integer targetUserAuthStatus;

    private Long handlerId;
    private String handlerName;
}
