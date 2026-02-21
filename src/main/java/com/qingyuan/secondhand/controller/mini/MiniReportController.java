package com.qingyuan.secondhand.controller.mini;

import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.ReportSubmitDTO;
import com.qingyuan.secondhand.service.ReportService;
import com.qingyuan.secondhand.vo.ReportDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mini/report")
@RequiredArgsConstructor
public class MiniReportController {

    private final ReportService reportService;

    @PostMapping("/submit")
    public Result<Void> submit(@RequestBody @Valid ReportSubmitDTO dto) {
        reportService.submitReport(dto);
        return Result.success();
    }

    @GetMapping("/detail/{id}")
    public Result<ReportDetailVO> detail(@PathVariable Long id) {
        return Result.success(reportService.getReportDetail(id));
    }
}
