package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.BannerDTO;
import com.qingyuan.secondhand.entity.Banner;
import com.qingyuan.secondhand.mapper.BannerMapper;
import com.qingyuan.secondhand.vo.BannerVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class BannerServiceImplTest {

    @Test
    void testGetMiniBannerList_CacheHit() throws Exception {
        BannerMapper bannerMapper = Mockito.mock(BannerMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        String cached = "[{\"id\":1,\"title\":\"A\"}]";
        Mockito.when(valueOperations.get(RedisConstant.BANNER_LIST + 1L)).thenReturn(cached);

        List<BannerVO> cachedList = List.of(buildVO(1L, "A"));
        Mockito.when(objectMapper.readValue(Mockito.eq(cached), Mockito.any(TypeReference.class))).thenReturn(cachedList);

        BannerServiceImpl service = new BannerServiceImpl(bannerMapper, stringRedisTemplate, objectMapper);
        List<BannerVO> result = service.getMiniBannerList(1L);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("A", result.get(0).getTitle());
        Mockito.verifyNoInteractions(bannerMapper);
    }

    @Test
    void testGetMiniBannerList_CacheMiss() throws Exception {
        BannerMapper bannerMapper = Mockito.mock(BannerMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get(RedisConstant.BANNER_LIST + 2L)).thenReturn(null);

        Banner banner = buildBanner(2L, "B", 2L);
        Mockito.when(bannerMapper.selectList(Mockito.any(LambdaQueryWrapper.class))).thenReturn(List.of(banner));
        Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("[{\"id\":2,\"title\":\"B\"}]");

        BannerServiceImpl service = new BannerServiceImpl(bannerMapper, stringRedisTemplate, objectMapper);
        List<BannerVO> result = service.getMiniBannerList(2L);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(2L, result.get(0).getId());

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TimeUnit> unitCaptor = ArgumentCaptor.forClass(TimeUnit.class);
        Mockito.verify(valueOperations).set(keyCaptor.capture(), jsonCaptor.capture(), ttlCaptor.capture(), unitCaptor.capture());

        Assertions.assertEquals(RedisConstant.BANNER_LIST + 2L, keyCaptor.getValue());
        Assertions.assertTrue(StringUtils.hasText(jsonCaptor.getValue()));
        Assertions.assertEquals(30L, ttlCaptor.getValue());
        Assertions.assertEquals(TimeUnit.MINUTES, unitCaptor.getValue());
    }

    @Test
    void testGetMiniBannerList_FilterByCampus() {
        BannerMapper bannerMapper = Mockito.mock(BannerMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get(RedisConstant.BANNER_LIST + 3L)).thenReturn(null);
        Mockito.when(bannerMapper.selectList(Mockito.any(LambdaQueryWrapper.class))).thenReturn(List.of(buildBanner(3L, "C", null)));
        try {
            Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("[]");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        BannerServiceImpl service = new BannerServiceImpl(bannerMapper, stringRedisTemplate, objectMapper);
        service.getMiniBannerList(3L);

        Mockito.verify(bannerMapper).selectList(Mockito.any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetAdminBannerPage_Success() {
        BannerMapper bannerMapper = Mockito.mock(BannerMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Page<Banner> pageResult = new Page<>(1, 10);
        Banner banner = buildBanner(4L, "D", 1L);
        pageResult.setRecords(List.of(banner));
        pageResult.setTotal(1);

        Mockito.when(bannerMapper.selectPage(Mockito.any(Page.class), Mockito.any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        BannerServiceImpl service = new BannerServiceImpl(bannerMapper, stringRedisTemplate, objectMapper);
        IPage<BannerVO> result = service.getAdminBannerPage(1, 10, 1, 1L);

        Assertions.assertEquals(1, result.getTotal());
        Assertions.assertEquals("D", result.getRecords().get(0).getTitle());
        Mockito.verify(bannerMapper).selectPage(Mockito.any(Page.class), Mockito.any(LambdaQueryWrapper.class));
    }

    @Test
    void testAddBanner_Success() {
        BannerMapper bannerMapper = Mockito.mock(BannerMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Mockito.when(bannerMapper.insert(Mockito.any(Banner.class))).thenReturn(1);
        Mockito.when(stringRedisTemplate.keys(RedisConstant.BANNER_LIST + "*")).thenReturn(Set.of(RedisConstant.BANNER_LIST + "1"));

        BannerServiceImpl service = new BannerServiceImpl(bannerMapper, stringRedisTemplate, objectMapper);
        BannerDTO dto = new BannerDTO();
        dto.setTitle("E");
        dto.setImage("e.png");
        dto.setLinkType(1);
        dto.setLinkUrl("url");
        dto.setCampusId(1L);
        dto.setSort(1);
        dto.setStatus(1);
        service.addBanner(dto);

        Mockito.verify(bannerMapper).insert(Mockito.any(Banner.class));
        Mockito.verify(stringRedisTemplate).delete(Mockito.anySet());
    }

    @Test
    void testUpdateBanner_Success() {
        BannerMapper bannerMapper = Mockito.mock(BannerMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Banner existing = buildBanner(10L, "F", 1L);
        Mockito.when(bannerMapper.selectById(10L)).thenReturn(existing);
        Mockito.when(bannerMapper.updateById(Mockito.any(Banner.class))).thenReturn(1);
        Mockito.when(stringRedisTemplate.keys(RedisConstant.BANNER_LIST + "*")).thenReturn(Set.of(RedisConstant.BANNER_LIST + "2"));

        BannerServiceImpl service = new BannerServiceImpl(bannerMapper, stringRedisTemplate, objectMapper);
        BannerDTO dto = new BannerDTO();
        dto.setTitle("F2");
        dto.setImage("f2.png");
        dto.setLinkType(2);
        dto.setLinkUrl("url2");
        dto.setCampusId(2L);
        dto.setSort(2);
        dto.setStatus(1);
        service.updateBanner(10L, dto);

        Mockito.verify(bannerMapper).updateById(Mockito.any(Banner.class));
        Mockito.verify(stringRedisTemplate).delete(Mockito.anySet());
    }

    @Test
    void testUpdateBanner_NotFound() {
        BannerMapper bannerMapper = Mockito.mock(BannerMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Mockito.when(bannerMapper.selectById(11L)).thenReturn(null);

        BannerServiceImpl service = new BannerServiceImpl(bannerMapper, stringRedisTemplate, objectMapper);
        BannerDTO dto = new BannerDTO();
        dto.setTitle("G");
        dto.setImage("g.png");
        dto.setSort(1);
        dto.setStatus(1);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.updateBanner(11L, dto));
        Assertions.assertEquals("Banner不存在", ex.getMsg());
    }

    @Test
    void testDeleteBanner_Success() {
        BannerMapper bannerMapper = Mockito.mock(BannerMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Banner existing = buildBanner(12L, "H", 1L);
        Mockito.when(bannerMapper.selectById(12L)).thenReturn(existing);
        Mockito.when(bannerMapper.deleteById(12L)).thenReturn(1);
        Mockito.when(stringRedisTemplate.keys(RedisConstant.BANNER_LIST + "*")).thenReturn(Set.of(RedisConstant.BANNER_LIST + "3"));

        BannerServiceImpl service = new BannerServiceImpl(bannerMapper, stringRedisTemplate, objectMapper);
        service.deleteBanner(12L);

        Mockito.verify(bannerMapper).deleteById(12L);
        Mockito.verify(stringRedisTemplate).delete(Mockito.anySet());
    }

    private Banner buildBanner(Long id, String title, Long campusId) {
        Banner banner = new Banner();
        banner.setId(id);
        banner.setTitle(title);
        banner.setImage("a.png");
        banner.setLinkType(1);
        banner.setLinkUrl("url");
        banner.setCampusId(campusId);
        banner.setSort(1);
        banner.setStatus(1);
        banner.setStartTime(LocalDateTime.now().minusDays(1));
        banner.setEndTime(LocalDateTime.now().plusDays(1));
        return banner;
    }

    private BannerVO buildVO(Long id, String title) {
        BannerVO vo = new BannerVO();
        vo.setId(id);
        vo.setTitle(title);
        return vo;
    }
}
