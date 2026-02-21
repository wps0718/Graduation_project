package com.qingyuan.secondhand.vo;

import lombok.Data;

@Data
public class CategoryVO {
    private Long id;
    private String name;
    private String icon;
    private Integer sort;
    private Integer status;
}
