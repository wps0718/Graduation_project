package com.qingyuan.secondhand.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.ReportHandleDTO;
import com.qingyuan.secondhand.service.ReportService;
import com.qingyuan.secondhand.vo.ReportDetailVO;
import com.qingyuan.secondhand.vo.ReportPageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/report")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping("/page")
    public Result<IPage<ReportPageVO>> page(@RequestParam Integer page,
                                            @RequestParam Integer pageSize,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) Integer targetType) {
        return Result.success(reportService.getReportPage(page, pageSize, status, targetType));
    }

    @GetMapping("/detail/{id}")
    public Result<ReportDetailVO> detail(@PathVariable Long id) {
        return Result.success(reportService.getReportDetail(id));
    }

    @PostMapping("/handle")
    public Result<Void> handle(@RequestBody @Valid ReportHandleDTO dto) {
        reportService.handleReport(dto, UserContext.getCurrentUserId());
        return Result.success();
    }
}
