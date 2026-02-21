package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.entity.SearchKeyword;
import com.qingyuan.secondhand.mapper.SearchKeywordMapper;
import com.qingyuan.secondhand.vo.HotKeywordVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class SearchKeywordServiceImplTest {

    @Test
    void testGetHotKeywords_CacheHit() throws Exception {
        SearchKeywordMapper searchKeywordMapper = Mockito.mock(SearchKeywordMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        String cached = "[{\"id\":1,\"keyword\":\"手机\"}]";
        Mockito.when(valueOperations.get(RedisConstant.SEARCH_HOT)).thenReturn(cached);

        List<HotKeywordVO> cachedList = List.of(buildVO(1L, "手机"));
        Mockito.when(objectMapper.readValue(Mockito.eq(cached), Mockito.any(TypeReference.class))).thenReturn(cachedList);

        SearchKeywordServiceImpl service = new SearchKeywordServiceImpl(searchKeywordMapper, stringRedisTemplate, objectMapper);
        List<HotKeywordVO> result = service.getHotKeywords();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("手机", result.get(0).getKeyword());
        Mockito.verifyNoInteractions(searchKeywordMapper);
    }

    @Test
    void testGetHotKeywords_OnlyHot() throws Exception {
        SearchKeywordMapper searchKeywordMapper = Mockito.mock(SearchKeywordMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get(RedisConstant.SEARCH_HOT)).thenReturn(null);

        Mockito.when(searchKeywordMapper.selectList(Mockito.any(LambdaQueryWrapper.class)))
                .thenReturn(buildHotList(10));
        Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("[]");

        SearchKeywordServiceImpl service = new SearchKeywordServiceImpl(searchKeywordMapper, stringRedisTemplate, objectMapper);
        List<HotKeywordVO> result = service.getHotKeywords();

        Assertions.assertEquals(10, result.size());
        Mockito.verify(searchKeywordMapper, Mockito.times(1)).selectList(Mockito.any(LambdaQueryWrapper.class));
        Mockito.verify(valueOperations).set(Mockito.eq(RedisConstant.SEARCH_HOT), Mockito.anyString(), Mockito.eq(1L), Mockito.eq(TimeUnit.HOURS));
    }

    @Test
    void testGetHotKeywords_HotAndCount() throws Exception {
        SearchKeywordMapper searchKeywordMapper = Mockito.mock(SearchKeywordMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get(RedisConstant.SEARCH_HOT)).thenReturn(null);

        Mockito.when(searchKeywordMapper.selectList(Mockito.any(LambdaQueryWrapper.class)))
                .thenReturn(buildHotList(5))
                .thenReturn(buildCountList(5));
        Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("[]");

        SearchKeywordServiceImpl service = new SearchKeywordServiceImpl(searchKeywordMapper, stringRedisTemplate, objectMapper);
        List<HotKeywordVO> result = service.getHotKeywords();

        Assertions.assertEquals(10, result.size());
        Mockito.verify(searchKeywordMapper, Mockito.times(2)).selectList(Mockito.any(LambdaQueryWrapper.class));
        Mockito.verify(valueOperations).set(Mockito.eq(RedisConstant.SEARCH_HOT), Mockito.anyString(), Mockito.eq(1L), Mockito.eq(TimeUnit.HOURS));
    }

    private List<SearchKeyword> buildHotList(int count) {
        return java.util.stream.IntStream.range(0, count).mapToObj(i -> {
            SearchKeyword keyword = new SearchKeyword();
            keyword.setId((long) i);
            keyword.setKeyword("热词" + i);
            keyword.setIsHot(1);
            keyword.setStatus(1);
            keyword.setSort(i);
            return keyword;
        }).toList();
    }

    private List<SearchKeyword> buildCountList(int count) {
        return java.util.stream.IntStream.range(0, count).mapToObj(i -> {
            SearchKeyword keyword = new SearchKeyword();
            keyword.setId((long) (100 + i));
            keyword.setKeyword("高频" + i);
            keyword.setIsHot(0);
            keyword.setStatus(1);
            keyword.setSearchCount(100 - i);
            return keyword;
        }).toList();
    }

    private HotKeywordVO buildVO(Long id, String keyword) {
        HotKeywordVO vo = new HotKeywordVO();
        vo.setId(id);
        vo.setKeyword(keyword);
        return vo;
    }
}
