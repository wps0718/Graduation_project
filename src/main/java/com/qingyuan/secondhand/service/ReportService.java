package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.dto.ReportHandleDTO;
import com.qingyuan.secondhand.dto.ReportSubmitDTO;
import com.qingyuan.secondhand.entity.Report;
import com.qingyuan.secondhand.vo.ReportDetailVO;
import com.qingyuan.secondhand.vo.ReportPageVO;

public interface ReportService extends IService<Report> {
    void submitReport(ReportSubmitDTO dto);

    IPage<ReportPageVO> getReportPage(Integer page, Integer pageSize, Integer status, Integer targetType);

    ReportDetailVO getReportDetail(Long id);

    void handleReport(ReportHandleDTO dto, Long handlerId);
}
