package com.qingyuan.secondhand.service;

import com.qingyuan.secondhand.vo.StatsCampusVO;
import com.qingyuan.secondhand.vo.StatsCategoryVO;
import com.qingyuan.secondhand.vo.StatsLoginMethodVO;
import com.qingyuan.secondhand.vo.StatsLoginTimeVO;
import com.qingyuan.secondhand.vo.StatsLoginTrendVO;
import com.qingyuan.secondhand.vo.StatsOverviewVO;
import com.qingyuan.secondhand.vo.StatsTrendVO;

import java.util.List;

public interface StatsService {
    StatsOverviewVO getOverview();

    List<StatsTrendVO> getTrend(Integer days);

    List<StatsCampusVO> getCampusStats();

    List<StatsCategoryVO> getCategoryStats();

    List<StatsLoginMethodVO> getLoginMethodStats(String startDate, String endDate);

    List<StatsLoginTimeVO> getLoginTimeStats(String startDate, String endDate);

    List<StatsLoginTrendVO> getLoginTrend(Integer days);
}
