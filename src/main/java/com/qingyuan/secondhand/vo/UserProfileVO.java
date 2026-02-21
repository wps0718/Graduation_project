package com.qingyuan.secondhand.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserProfileVO {
    private Long id;
    private String nickName;
    private String avatarUrl;
    private Integer authStatus;
    private BigDecimal score;
    private Integer onSaleCount;
    private Integer soldCount;
    private Page<ProductSimpleVO> products;
}
