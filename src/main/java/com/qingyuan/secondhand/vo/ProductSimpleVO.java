package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductSimpleVO {
    private Long id;
    private String title;
    private BigDecimal price;
    private List<String> images;
    private LocalDateTime createTime;
}
