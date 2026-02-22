package com.qingyuan.secondhand.mapper;

import com.qingyuan.secondhand.vo.StatsCampusVO;
import com.qingyuan.secondhand.vo.StatsCategoryVO;
import com.qingyuan.secondhand.vo.StatsTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

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
}
