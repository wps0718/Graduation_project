package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.entity.BrowseHistory;
import com.qingyuan.secondhand.mapper.BrowseHistoryMapper;
import com.qingyuan.secondhand.service.BrowseHistoryService;
import com.qingyuan.secondhand.vo.FootprintGroupVO;
import com.qingyuan.secondhand.vo.FootprintItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.qingyuan.secondhand.common.util.ImageJsonUtil.parseCoverImage;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrowseHistoryServiceImpl extends ServiceImpl<BrowseHistoryMapper, BrowseHistory> implements BrowseHistoryService {

    private final BrowseHistoryMapper browseHistoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordBrowse(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return;
        }
        BrowseHistory history = new BrowseHistory();
        history.setUserId(userId);
        history.setProductId(productId);
        history.setIsDeleted(0);
        try {
            browseHistoryMapper.insert(history);
        } catch (DuplicateKeyException e) {
            browseHistoryMapper.update(null, new LambdaUpdateWrapper<BrowseHistory>()
                    .eq(BrowseHistory::getUserId, userId)
                    .eq(BrowseHistory::getProductId, productId)
                    .set(BrowseHistory::getCreateTime, LocalDateTime.now())
                    .set(BrowseHistory::getIsDeleted, 0));
        }
    }

    @Override
    public List<FootprintGroupVO> getFootprintList(Integer page, Integer pageSize, Long categoryId,
                                                   String beginTime, String endTime) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        LocalDateTime begin = null;
        LocalDateTime end = null;
        if (beginTime != null && !beginTime.isEmpty()) {
            begin = LocalDate.parse(beginTime).atStartOfDay();
        }
        if (endTime != null && !endTime.isEmpty()) {
            end = LocalDate.parse(endTime).atTime(LocalTime.MAX);
        }
        // 未指定起始时间时，默认只查最近 90 天，防止全表加载
        if (begin == null) {
            begin = LocalDate.now().minusDays(90).atStartOfDay();
        }

        List<FootprintItemVO> allRecords = browseHistoryMapper.getFootprintListForGroup(
                userId, categoryId, begin, end);

        if (allRecords == null || allRecords.isEmpty()) {
            return new ArrayList<>();
        }

        allRecords.forEach(item -> {
            item.setCoverImage(parseCoverImage(item.getCoverImage()));
            item.setStatusText(resolveStatusText(item.getStatus()));
        });

        Map<String, List<FootprintItemVO>> grouped = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minus(1, ChronoUnit.DAYS);

        for (FootprintItemVO item : allRecords) {
            LocalDateTime browseTime = item.getBrowseTime();
            if (browseTime == null) continue;

            String groupKey;
            LocalDate browseDate = browseTime.toLocalDate();
            if (browseDate.equals(today)) {
                groupKey = "今天";
            } else if (browseDate.equals(yesterday)) {
                groupKey = "昨天";
            } else if (browseDate.getYear() == today.getYear()) {
                groupKey = browseDate.format(DateTimeFormatter.ofPattern("M月d日"));
            } else {
                groupKey = browseDate.format(DateTimeFormatter.ofPattern("yyyy年M月d日"));
            }

            grouped.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(item);
        }

        List<FootprintGroupVO> groupList = new ArrayList<>();
        for (Map.Entry<String, List<FootprintItemVO>> entry : grouped.entrySet()) {
            FootprintGroupVO group = new FootprintGroupVO();
            group.setDate(entry.getKey());
            group.setItems(entry.getValue());
            groupList.add(group);
        }

        int total = groupList.size();
        int startIdx = (page - 1) * pageSize;
        int endIdx = Math.min(startIdx + pageSize, total);

        if (startIdx >= total) {
            return new ArrayList<>();
        }

        return groupList.subList(startIdx, endIdx);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFootprints(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的足迹");
        }
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        browseHistoryMapper.delete(new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId)
                .in(BrowseHistory::getId, ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearFootprints() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        browseHistoryMapper.delete(new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId));
    }

    @Override
    public Long getFootprintCount() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return 0L;
        }
        return browseHistoryMapper.selectCount(new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId));
    }

    // 修复：带筛选条件的足迹总数，与 getFootprintList 使用相同的 WHERE 条件
    // 用于前端导航栏标题显示正确的筛选后数量
    @Override
    public Long getFootprintCount(Long categoryId, String beginTime, String endTime) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return 0L;
        }
        LocalDateTime begin = null;
        LocalDateTime end = null;
        if (beginTime != null && !beginTime.isEmpty()) {
            begin = LocalDate.parse(beginTime).atStartOfDay();
        }
        if (endTime != null && !endTime.isEmpty()) {
            end = LocalDate.parse(endTime).atTime(LocalTime.MAX);
        }
        if (begin == null) {
            begin = LocalDate.now().minusDays(90).atStartOfDay();
        }
        Long count = browseHistoryMapper.getFootprintCountFiltered(userId, categoryId, begin, end);
        return count != null ? count : 0L;
    }

    // 修复：status 2 和 3 的文案对调了
    // Product 状态枚举：0-待审核 / 1-在售 / 2-已下架 / 3-已售出 / 4-审核驳回
    private String resolveStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审核";
            case 1: return "在售";
            case 2: return "已下架";   // 修复：之前误写为"已售出"
            case 3: return "已售出";   // 修复：之前误写为"已下架"
            case 4: return "审核不通过";
            default: return "未知";
        }
    }
}
