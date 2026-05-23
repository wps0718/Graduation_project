package com.qingyuan.secondhand.service.impl;

import com.qingyuan.secondhand.mapper.StatsMapper;
import com.qingyuan.secondhand.service.StatsService;
import com.qingyuan.secondhand.vo.StatsCampusVO;
import com.qingyuan.secondhand.vo.StatsCategoryVO;
import com.qingyuan.secondhand.vo.StatsOverviewVO;
import com.qingyuan.secondhand.vo.StatsTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsMapper statsMapper;

    @Override
    public StatsOverviewVO getOverview() {
        // 合并为 2 条 SQL，替代原来的 11 条
        Map<String, Object> agg = statsMapper.getOverviewAggregates();
        Map<String, Object> pending = statsMapper.getPendingCounts();

        StatsOverviewVO vo = new StatsOverviewVO();
        vo.setTodayNewUsers(toInt(agg.get("todayNewUsers")));
        vo.setTodayNewProducts(toInt(agg.get("todayNewProducts")));
        vo.setTodayNewOrders(toInt(agg.get("todayNewOrders")));
        vo.setTodayGmv(toBigDecimal(agg.get("todayGmv")));
        vo.setTotalUsers(toInt(agg.get("totalUsers")));
        vo.setTotalProducts(toInt(agg.get("totalProducts")));
        vo.setTotalOrders(toInt(agg.get("totalOrders")));
        vo.setTotalAmount(toBigDecimal(agg.get("totalAmount")));
        vo.setPendingProductCount(toInt(pending.get("pendingProductCount")));
        vo.setPendingAuthCount(toInt(pending.get("pendingAuthCount")));
        vo.setPendingReports(toInt(pending.get("pendingReports")));
        return vo;
    }

    @Override
    public List<StatsTrendVO> getTrend(Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }
        if (days > 30) {
            days = 30;
        }
        return statsMapper.getTrendData(days);
    }

    @Override
    public List<StatsCampusVO> getCampusStats() {
        return statsMapper.getCampusStats();
    }

    @Override
    public List<StatsCategoryVO> getCategoryStats() {
        return statsMapper.getCategoryStats();
    }

    private Integer toInt(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        return 0;
    }

    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        if (obj instanceof Number) return BigDecimal.valueOf(((Number) obj).doubleValue());
        return BigDecimal.ZERO;
    }
}
