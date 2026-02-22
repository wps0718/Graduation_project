package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatsOverviewVO {
    private Integer todayNewUsers;
    private Integer todayNewProducts;
    private Integer todayOrders;
    private BigDecimal todayGmv;
    private Integer totalUsers;
    private Integer totalProducts;
    private Integer totalOrders;
    private BigDecimal totalGmv;
    private Integer pendingProducts;
    private Integer pendingAuths;
    private Integer pendingReports;
}
