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
    private String bio;
    private String ipRegion;
    private Integer lastActiveDays;
    private String lastActiveText;
    private Long followerCount;
    private Long followingCount;
    private Integer onSaleCount;
    private Integer soldCount;
    private Integer status; // 0-正常 1-封禁 2-注销
    private Page<SellerProductVO> products;
}
