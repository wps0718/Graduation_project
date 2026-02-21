package com.qingyuan.secondhand.vo;

import lombok.Data;

@Data
public class HotKeywordVO {
    private Long id;
    private String keyword;
    private Integer searchCount;
    private Integer isHot;
    private Integer sort;
}
