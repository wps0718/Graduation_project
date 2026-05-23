package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.entity.BrowseHistory;
import com.qingyuan.secondhand.vo.FootprintGroupVO;

import java.util.List;

public interface BrowseHistoryService extends IService<BrowseHistory> {
    void recordBrowse(Long userId, Long productId);

    List<FootprintGroupVO> getFootprintList(Integer page, Integer pageSize, Long categoryId,
                                            String beginTime, String endTime);

    void deleteFootprints(List<Long> ids);

    void clearFootprints();

    Long getFootprintCount();

    // 修复：带筛选条件的足迹总数查询
    Long getFootprintCount(Long categoryId, String beginTime, String endTime);
}
