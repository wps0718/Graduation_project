package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.entity.SearchKeyword;
import com.qingyuan.secondhand.mapper.SearchKeywordMapper;
import com.qingyuan.secondhand.service.SearchKeywordService;
import com.qingyuan.secondhand.vo.HotKeywordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SearchKeywordServiceImpl extends ServiceImpl<SearchKeywordMapper, SearchKeyword> implements SearchKeywordService {

    private final SearchKeywordMapper searchKeywordMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<HotKeywordVO> getHotKeywords() {
        String cacheKey = RedisConstant.SEARCH_HOT;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<HotKeywordVO>>() {
                });
            } catch (Exception e) {
                stringRedisTemplate.delete(cacheKey);
            }
        }

        LambdaQueryWrapper<SearchKeyword> hotWrapper = new LambdaQueryWrapper<>();
        hotWrapper.eq(SearchKeyword::getStatus, 1)
                .eq(SearchKeyword::getIsHot, 1)
                .orderByAsc(SearchKeyword::getSort);
        List<SearchKeyword> hotList = searchKeywordMapper.selectList(hotWrapper);
        List<SearchKeyword> resultList = new ArrayList<>(hotList == null ? List.of() : hotList);

        if (resultList.size() < 10) {
            int need = 10 - resultList.size();
            LambdaQueryWrapper<SearchKeyword> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(SearchKeyword::getStatus, 1)
                    .eq(SearchKeyword::getIsHot, 0)
                    .orderByDesc(SearchKeyword::getSearchCount)
                    .last("LIMIT " + need);
            List<SearchKeyword> countList = searchKeywordMapper.selectList(countWrapper);
            if (countList != null && !countList.isEmpty()) {
                resultList.addAll(countList);
            }
        }

        List<HotKeywordVO> result = resultList.stream().limit(10).map(this::toHotKeywordVO).toList();

        try {
            String json = objectMapper.writeValueAsString(result);
            stringRedisTemplate.opsForValue().set(cacheKey, json, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            return result;
        }

        return result;
    }

    private HotKeywordVO toHotKeywordVO(SearchKeyword keyword) {
        HotKeywordVO vo = new HotKeywordVO();
        vo.setId(keyword.getId());
        vo.setKeyword(keyword.getKeyword());
        vo.setSearchCount(keyword.getSearchCount());
        vo.setIsHot(keyword.getIsHot());
        vo.setSort(keyword.getSort());
        return vo;
    }
}
