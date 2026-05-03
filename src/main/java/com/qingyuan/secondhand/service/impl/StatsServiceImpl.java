package com.qingyuan.secondhand.service.impl;

import com.qingyuan.secondhand.mapper.StatsMapper;
import com.qingyuan.secondhand.service.StatsService;
import com.qingyuan.secondhand.vo.StatsCampusVO;
import com.qingyuan.secondhand.vo.StatsCategoryVO;
import com.qingyuan.secondhand.vo.StatsOverviewVO;
import com.qingyuan.secondhand.vo.StatsTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsMapper statsMapper;

    @Override
    public StatsOverviewVO getOverview() {
        StatsOverviewVO vo = new StatsOverviewVO();
        vo.setTodayNewUsers(statsMapper.countTodayNewUsers());
        vo.setTodayNewProducts(statsMapper.countTodayNewProducts());
        vo.setTodayNewOrders(statsMapper.countTodayOrders());
        vo.setTodayGmv(statsMapper.sumTodayGmv());
        vo.setTotalUsers(statsMapper.countTotalUsers());
        vo.setTotalProducts(statsMapper.countTotalProducts());
        vo.setTotalOrders(statsMapper.countTotalOrders());
        vo.setTotalAmount(statsMapper.sumTotalGmv());
        vo.setPendingProductCount(statsMapper.countPendingProducts());
        vo.setPendingAuthCount(statsMapper.countPendingAuths());
        vo.setPendingReports(statsMapper.countPendingReports());
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
}
