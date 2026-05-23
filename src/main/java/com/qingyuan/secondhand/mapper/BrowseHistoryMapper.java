package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.entity.BrowseHistory;
import com.qingyuan.secondhand.vo.FootprintItemVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BrowseHistoryMapper extends BaseMapper<BrowseHistory> {
    Page<FootprintItemVO> getFootprintList(Page<FootprintItemVO> page, @Param("userId") Long userId,
                                           @Param("categoryId") Long categoryId,
                                           @Param("beginTime") LocalDateTime beginTime,
                                           @Param("endTime") LocalDateTime endTime);

    List<FootprintItemVO> getFootprintListForGroup(@Param("userId") Long userId,
                                                   @Param("categoryId") Long categoryId,
                                                   @Param("beginTime") LocalDateTime beginTime,
                                                   @Param("endTime") LocalDateTime endTime);

    // 修复：带筛选条件的足迹总数，用于前端导航栏标题和分页判断
    Long getFootprintCountFiltered(@Param("userId") Long userId,
                                   @Param("categoryId") Long categoryId,
                                   @Param("beginTime") LocalDateTime beginTime,
                                   @Param("endTime") LocalDateTime endTime);
}
