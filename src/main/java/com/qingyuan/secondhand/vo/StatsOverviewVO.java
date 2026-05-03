package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatsOverviewVO {
    private Integer todayNewUsers;
    private Integer todayNewProducts;
    private Integer todayNewOrders;
    private BigDecimal todayGmv;
    private Integer totalUsers;
    private Integer totalProducts;
    private Integer totalOrders;
    private BigDecimal totalAmount;
    private Integer pendingProductCount;
    private Integer pendingAuthCount;
    private Integer pendingReports;
}
