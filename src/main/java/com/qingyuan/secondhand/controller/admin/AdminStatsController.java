package com.qingyuan.secondhand.controller.admin;

import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.service.StatsService;
import com.qingyuan.secondhand.vo.StatsCampusVO;
import com.qingyuan.secondhand.vo.StatsCategoryVO;
import com.qingyuan.secondhand.vo.StatsLoginMethodVO;
import com.qingyuan.secondhand.vo.StatsLoginTimeVO;
import com.qingyuan.secondhand.vo.StatsLoginTrendVO;
import com.qingyuan.secondhand.vo.StatsOverviewVO;
import com.qingyuan.secondhand.vo.StatsTrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "管理端-统计")
@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    public Result<StatsOverviewVO> getOverview() {
        return Result.success(statsService.getOverview());
    }

    @GetMapping("/trend")
    public Result<List<StatsTrendVO>> getTrend(@RequestParam(required = false) Integer days) {
        return Result.success(statsService.getTrend(days));
    }

    @GetMapping("/campus")
    public Result<List<StatsCampusVO>> getCampusStats() {
        return Result.success(statsService.getCampusStats());
    }

    @GetMapping("/category")
    public Result<List<StatsCategoryVO>> getCategoryStats() {
        return Result.success(statsService.getCategoryStats());
    }

    @GetMapping("/login-method")
    public Result<List<StatsLoginMethodVO>> getLoginMethodStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(statsService.getLoginMethodStats(startDate, endDate));
    }

    @GetMapping("/login-time")
    public Result<List<StatsLoginTimeVO>> getLoginTimeStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(statsService.getLoginTimeStats(startDate, endDate));
    }

    @GetMapping("/login-trend")
    public Result<List<StatsLoginTrendVO>> getLoginTrend(@RequestParam(required = false) Integer days) {
        return Result.success(statsService.getLoginTrend(days));
    }
}
