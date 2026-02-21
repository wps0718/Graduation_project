package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDetailVO {
    private Long myReviewId;
    private Integer myScoreDesc;
    private Integer myScoreAttitude;
    private Integer myScoreExperience;
    private String myContent;
    private LocalDateTime myCreateTime;

    private Long otherReviewId;
    private Integer otherScoreDesc;
    private Integer otherScoreAttitude;
    private Integer otherScoreExperience;
    private String otherContent;
    private LocalDateTime otherCreateTime;

    private Integer orderStatus;
}
