package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatsTrendVO {
    private String date;
    private Integer newUsers;
    private Integer newProducts;
    private Integer orders;
    private BigDecimal gmv;
}
