package com.qingyuan.secondhand.vo;

import lombok.Data;

@Data
public class StatsCampusVO {
    private Long campusId;
    private String campusName;
    private Integer productCount;
    private Integer orderCount;
    private Integer userCount;
}
