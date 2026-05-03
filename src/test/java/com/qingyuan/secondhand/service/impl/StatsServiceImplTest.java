package com.qingyuan.secondhand.service.impl;

import com.qingyuan.secondhand.mapper.StatsMapper;
import com.qingyuan.secondhand.vo.StatsCampusVO;
import com.qingyuan.secondhand.vo.StatsCategoryVO;
import com.qingyuan.secondhand.vo.StatsOverviewVO;
import com.qingyuan.secondhand.vo.StatsTrendVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class StatsServiceImplTest {

    @Test
    void testGetOverview_Success() {
        StatsMapper statsMapper = Mockito.mock(StatsMapper.class);
        StatsServiceImpl service = new StatsServiceImpl(statsMapper);

        Mockito.when(statsMapper.countTodayNewUsers()).thenReturn(3);
        Mockito.when(statsMapper.countTodayNewProducts()).thenReturn(5);
        Mockito.when(statsMapper.countTodayOrders()).thenReturn(2);
        Mockito.when(statsMapper.sumTodayGmv()).thenReturn(new BigDecimal("123.45"));
        Mockito.when(statsMapper.countTotalUsers()).thenReturn(100);
        Mockito.when(statsMapper.countTotalProducts()).thenReturn(80);
        Mockito.when(statsMapper.countTotalOrders()).thenReturn(60);
        Mockito.when(statsMapper.sumTotalGmv()).thenReturn(new BigDecimal("4567.89"));
        Mockito.when(statsMapper.countPendingProducts()).thenReturn(7);
        Mockito.when(statsMapper.countPendingAuths()).thenReturn(4);
        Mockito.when(statsMapper.countPendingReports()).thenReturn(1);

        StatsOverviewVO result = service.getOverview();

        Assertions.assertEquals(3, result.getTodayNewUsers());
        Assertions.assertEquals(5, result.getTodayNewProducts());
        Assertions.assertEquals(2, result.getTodayNewOrders());
        Assertions.assertEquals(new BigDecimal("123.45"), result.getTodayGmv());
        Assertions.assertEquals(100, result.getTotalUsers());
        Assertions.assertEquals(80, result.getTotalProducts());
        Assertions.assertEquals(60, result.getTotalOrders());
        Assertions.assertEquals(new BigDecimal("4567.89"), result.getTotalAmount());
        Assertions.assertEquals(7, result.getPendingProductCount());
        Assertions.assertEquals(4, result.getPendingAuthCount());
        Assertions.assertEquals(1, result.getPendingReports());
    }

    @Test
    void testGetTrend_DefaultDays() {
        StatsMapper statsMapper = Mockito.mock(StatsMapper.class);
        StatsServiceImpl service = new StatsServiceImpl(statsMapper);

        List<StatsTrendVO> trendList = List.of(buildTrend("2026-02-20", 1, 2, 3, "10.00"));
        Mockito.when(statsMapper.getTrendData(7)).thenReturn(trendList);

        List<StatsTrendVO> result = service.getTrend(null);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("2026-02-20", result.get(0).getDate());
        Mockito.verify(statsMapper).getTrendData(7);
    }

    @Test
    void testGetTrend_CustomDays() {
        StatsMapper statsMapper = Mockito.mock(StatsMapper.class);
        StatsServiceImpl service = new StatsServiceImpl(statsMapper);

        List<StatsTrendVO> trendList = List.of(buildTrend("2026-02-18", 2, 1, 0, "0.00"));
        Mockito.when(statsMapper.getTrendData(14)).thenReturn(trendList);

        List<StatsTrendVO> result = service.getTrend(14);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("2026-02-18", result.get(0).getDate());
        Mockito.verify(statsMapper).getTrendData(14);
    }

    @Test
    void testGetTrend_InvalidDays() {
        StatsMapper statsMapper = Mockito.mock(StatsMapper.class);
        StatsServiceImpl service = new StatsServiceImpl(statsMapper);

        List<StatsTrendVO> trendList = List.of(buildTrend("2026-02-19", 0, 0, 0, "0.00"));
        Mockito.when(statsMapper.getTrendData(7)).thenReturn(trendList);

        List<StatsTrendVO> result = service.getTrend(0);

        Assertions.assertEquals(1, result.size());
        Mockito.verify(statsMapper).getTrendData(7);
    }

    @Test
    void testGetTrend_ExceedMaxDays() {
        StatsMapper statsMapper = Mockito.mock(StatsMapper.class);
        StatsServiceImpl service = new StatsServiceImpl(statsMapper);

        List<StatsTrendVO> trendList = List.of(buildTrend("2026-02-17", 3, 4, 5, "88.00"));
        Mockito.when(statsMapper.getTrendData(30)).thenReturn(trendList);

        List<StatsTrendVO> result = service.getTrend(50);

        Assertions.assertEquals(1, result.size());
        Mockito.verify(statsMapper).getTrendData(30);
    }

    @Test
    void testGetCampusStats_Success() {
        StatsMapper statsMapper = Mockito.mock(StatsMapper.class);
        StatsServiceImpl service = new StatsServiceImpl(statsMapper);

        StatsCampusVO campus = new StatsCampusVO();
        campus.setCampusId(1L);
        campus.setCampusName("南海北");
        campus.setProductCount(10);
        campus.setOrderCount(6);
        campus.setUserCount(200);
        Mockito.when(statsMapper.getCampusStats()).thenReturn(List.of(campus));

        List<StatsCampusVO> result = service.getCampusStats();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("南海北", result.get(0).getCampusName());
        Assertions.assertEquals(10, result.get(0).getProductCount());
    }

    @Test
    void testGetCategoryStats_Success() {
        StatsMapper statsMapper = Mockito.mock(StatsMapper.class);
        StatsServiceImpl service = new StatsServiceImpl(statsMapper);

        StatsCategoryVO category = new StatsCategoryVO();
        category.setCategoryId(2L);
        category.setCategoryName("数码");
        category.setProductCount(12);
        category.setOrderCount(4);
        Mockito.when(statsMapper.getCategoryStats()).thenReturn(List.of(category));

        List<StatsCategoryVO> result = service.getCategoryStats();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("数码", result.get(0).getCategoryName());
        Assertions.assertEquals(12, result.get(0).getProductCount());
    }

    private StatsTrendVO buildTrend(String date, int newUsers, int newProducts, int orders, String gmv) {
        StatsTrendVO vo = new StatsTrendVO();
        vo.setDate(date);
        vo.setNewUsers(newUsers);
        vo.setNewProducts(newProducts);
        vo.setOrders(orders);
        vo.setGmv(new BigDecimal(gmv));
        return vo;
    }
}
