package com.qingyuan.secondhand.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.NoticeDTO;
import com.qingyuan.secondhand.service.NoticeService;
import com.qingyuan.secondhand.vo.NoticeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/notice")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticeService noticeService;

    @GetMapping("/page")
    public Result<Page<NoticeVO>> getNoticePage(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                @RequestParam(required = false) Integer type,
                                                @RequestParam(required = false) Integer status) {
        Page<NoticeVO> result = noticeService.getNoticePage(page, pageSize, type, status);
        return Result.success(result);
    }

    @PostMapping("/add")
    public Result<Void> addNotice(@RequestBody @Valid NoticeDTO dto) {
        noticeService.addNotice(dto);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> updateNotice(@RequestBody @Valid NoticeDTO dto) {
        noticeService.updateNotice(dto);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteNotice(@RequestParam Long id) {
        noticeService.deleteNotice(id);
        return Result.success();
    }
}
