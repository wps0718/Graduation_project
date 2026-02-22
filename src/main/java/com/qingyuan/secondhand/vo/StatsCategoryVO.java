package com.qingyuan.secondhand.vo;

import lombok.Data;

@Data
public class StatsCategoryVO {
    private Long categoryId;
    private String categoryName;
    private Integer productCount;
    private Integer orderCount;
}
