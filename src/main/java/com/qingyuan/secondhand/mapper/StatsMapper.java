package com.qingyuan.secondhand.mapper;

import com.qingyuan.secondhand.vo.StatsCampusVO;
import com.qingyuan.secondhand.vo.StatsCategoryVO;
import com.qingyuan.secondhand.vo.StatsLoginMethodVO;
import com.qingyuan.secondhand.vo.StatsLoginTimeVO;
import com.qingyuan.secondhand.vo.StatsLoginTrendVO;
import com.qingyuan.secondhand.vo.StatsTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatsMapper {
    Integer countTodayNewUsers();

    Integer countTodayNewProducts();

    Integer countTodayOrders();

    BigDecimal sumTodayGmv();

    Integer countTotalUsers();

    Integer countTotalProducts();

    Integer countTotalOrders();

    BigDecimal sumTotalGmv();

    Integer countPendingProducts();

    Integer countPendingAuths();

    Integer countPendingReports();

    List<StatsTrendVO> getTrendData(@Param("days") Integer days);

    List<StatsCampusVO> getCampusStats();

    List<StatsCategoryVO> getCategoryStats();

    Map<String, Object> getOverviewAggregates();

    Map<String, Object> getPendingCounts();

    List<StatsLoginMethodVO> getLoginMethodStats(@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<StatsLoginTimeVO> getLoginTimeStats(@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<StatsLoginTrendVO> getLoginTrend(@Param("days") Integer days);
}
