package com.qingyuan.secondhand.vo;

import lombok.Data;

@Data
public class StatsLoginTrendVO {
    private String date;
    private Integer loginCount;
    private Integer registerCount;
}
