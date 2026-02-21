package com.qingyuan.secondhand.controller.mini;

import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.service.SearchKeywordService;
import com.qingyuan.secondhand.vo.HotKeywordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mini/search")
@RequiredArgsConstructor
public class MiniSearchController {

    private final SearchKeywordService searchKeywordService;

    @GetMapping("/hot-keywords")
    public Result<List<HotKeywordVO>> getHotKeywords() {
        List<HotKeywordVO> list = searchKeywordService.getHotKeywords();
        return Result.success(list);
    }
}
