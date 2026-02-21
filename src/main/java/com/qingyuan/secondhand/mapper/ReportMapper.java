package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingyuan.secondhand.entity.Report;
import com.qingyuan.secondhand.vo.ReportDetailVO;
import com.qingyuan.secondhand.vo.ReportPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReportMapper extends BaseMapper<Report> {
    IPage<ReportPageVO> getReportPage(Page<ReportPageVO> page,
                                      @Param("status") Integer status,
                                      @Param("targetType") Integer targetType);

    ReportDetailVO getReportDetailById(@Param("id") Long id);
}
