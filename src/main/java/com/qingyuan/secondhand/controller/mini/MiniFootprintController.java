package com.qingyuan.secondhand.controller.mini;

import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.FootprintDeleteDTO;
import com.qingyuan.secondhand.service.BrowseHistoryService;
import com.qingyuan.secondhand.vo.FootprintGroupVO;
import com.qingyuan.secondhand.vo.FootprintListVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "小程序-足迹")
@RestController
@RequestMapping("/mini/footprint")
@RequiredArgsConstructor
public class MiniFootprintController {

    private final BrowseHistoryService browseHistoryService;

    @GetMapping("/list")
    public Result<FootprintListVO> getFootprintList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {
        List<FootprintGroupVO> records = browseHistoryService.getFootprintList(page, pageSize, categoryId, beginTime, endTime);
        // 修复：total 必须带上筛选条件，否则切换分类/时间后导航栏数字不变
        long total = browseHistoryService.getFootprintCount(categoryId, beginTime, endTime);
        FootprintListVO result = new FootprintListVO();
        result.setRecords(records);
        result.setTotal(total);
        return Result.success(result);
    }

    @PostMapping("/delete")
    public Result<Void> deleteFootprints(@RequestBody @Valid FootprintDeleteDTO dto) {
        browseHistoryService.deleteFootprints(dto.getIds());
        return Result.success();
    }

    @PostMapping("/clear")
    public Result<Void> clearFootprints() {
        browseHistoryService.clearFootprints();
        return Result.success();
    }

    @GetMapping("/count")
    public Result<Long> getFootprintCount() {
        return Result.success(browseHistoryService.getFootprintCount());
    }

    @PostMapping("/record")
    public Result<Void> recordFootprint(@RequestParam Long productId) {
        Long userId = com.qingyuan.secondhand.common.context.UserContext.getCurrentUserId();
        if (userId != null) {
            browseHistoryService.recordBrowse(userId, productId);
        }
        return Result.success();
    }
}
