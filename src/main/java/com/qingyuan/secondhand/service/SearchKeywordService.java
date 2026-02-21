package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.entity.SearchKeyword;
import com.qingyuan.secondhand.vo.HotKeywordVO;

import java.util.List;

public interface SearchKeywordService extends IService<SearchKeyword> {
    List<HotKeywordVO> getHotKeywords();
}
