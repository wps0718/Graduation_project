package com.qingyuan.secondhand.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.CampusDTO;
import com.qingyuan.secondhand.entity.Campus;
import com.qingyuan.secondhand.mapper.CampusMapper;
import com.qingyuan.secondhand.vo.CampusVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class CampusServiceImplTest {

    @Test
    void testGetMiniList_CacheHit() throws Exception {
        CampusMapper campusMapper = Mockito.mock(CampusMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        String json = "[{\"id\":1,\"name\":\"南校区\"}]";
        Mockito.when(valueOperations.get(RedisConstant.CAMPUS_LIST)).thenReturn(json);

        List<CampusVO> cachedList = List.of(buildVO(1L, "南校区"));
        Mockito.when(objectMapper.readValue(Mockito.eq(json), Mockito.any(TypeReference.class))).thenReturn(cachedList);

        CampusServiceImpl service = new CampusServiceImpl(campusMapper, stringRedisTemplate, objectMapper);
        List<CampusVO> result = service.getMiniList();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("南校区", result.get(0).getName());
        Mockito.verifyNoInteractions(campusMapper);
    }

    @Test
    void testGetMiniList_CacheMiss() throws Exception {
        CampusMapper campusMapper = Mockito.mock(CampusMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get(RedisConstant.CAMPUS_LIST)).thenReturn(null);

        Campus campus = new Campus();
        campus.setId(2L);
        campus.setName("北校区");
        campus.setCode("north");
        campus.setSort(1);
        campus.setStatus(1);
        Mockito.when(campusMapper.selectList(Mockito.any())).thenReturn(List.of(campus));

        Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("[{\"id\":2,\"name\":\"北校区\"}]");

        CampusServiceImpl service = new CampusServiceImpl(campusMapper, stringRedisTemplate, objectMapper);
        List<CampusVO> result = service.getMiniList();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(2L, result.get(0).getId());

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TimeUnit> unitCaptor = ArgumentCaptor.forClass(TimeUnit.class);
        Mockito.verify(valueOperations).set(keyCaptor.capture(), jsonCaptor.capture(), ttlCaptor.capture(), unitCaptor.capture());

        Assertions.assertEquals(RedisConstant.CAMPUS_LIST, keyCaptor.getValue());
        Assertions.assertTrue(StringUtils.hasText(jsonCaptor.getValue()));
        Assertions.assertEquals(1L, ttlCaptor.getValue());
        Assertions.assertEquals(TimeUnit.HOURS, unitCaptor.getValue());
    }

    @Test
    void testGetAdminList() {
        CampusMapper campusMapper = Mockito.mock(CampusMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Campus campus = new Campus();
        campus.setId(3L);
        campus.setName("东校区");
        campus.setSort(1);
        campus.setStatus(1);
        Mockito.when(campusMapper.selectList(Mockito.any())).thenReturn(List.of(campus));

        CampusServiceImpl service = new CampusServiceImpl(campusMapper, stringRedisTemplate, objectMapper);
        List<CampusVO> result = service.getAdminList();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("东校区", result.get(0).getName());
    }

    @Test
    void testAddCampus() {
        CampusMapper campusMapper = Mockito.mock(CampusMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Mockito.when(campusMapper.insert(Mockito.any(Campus.class))).thenReturn(1);

        CampusServiceImpl service = new CampusServiceImpl(campusMapper, stringRedisTemplate, objectMapper);
        CampusDTO dto = new CampusDTO();
        dto.setName("新校区");
        dto.setCode("new_campus");
        dto.setSort(1);
        dto.setStatus(1);
        service.addCampus(dto);

        Mockito.verify(campusMapper).insert(Mockito.any(Campus.class));
        Mockito.verify(stringRedisTemplate).delete(RedisConstant.CAMPUS_LIST);
    }

    @Test
    void testUpdateCampus_Success() {
        CampusMapper campusMapper = Mockito.mock(CampusMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Campus existing = new Campus();
        existing.setId(10L);
        Mockito.when(campusMapper.selectById(10L)).thenReturn(existing);
        Mockito.when(campusMapper.updateById(Mockito.any(Campus.class))).thenReturn(1);

        CampusServiceImpl service = new CampusServiceImpl(campusMapper, stringRedisTemplate, objectMapper);
        CampusDTO dto = new CampusDTO();
        dto.setId(10L);
        dto.setName("南校区");
        dto.setCode("south");
        dto.setSort(1);
        dto.setStatus(1);
        service.updateCampus(dto);

        Mockito.verify(campusMapper).updateById(Mockito.any(Campus.class));
        Mockito.verify(stringRedisTemplate).delete(RedisConstant.CAMPUS_LIST);
    }

    @Test
    void testUpdateCampus_NotFound() {
        CampusMapper campusMapper = Mockito.mock(CampusMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Mockito.when(campusMapper.selectById(11L)).thenReturn(null);

        CampusServiceImpl service = new CampusServiceImpl(campusMapper, stringRedisTemplate, objectMapper);
        CampusDTO dto = new CampusDTO();
        dto.setId(11L);
        dto.setName("南校区");
        dto.setSort(1);
        dto.setStatus(1);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.updateCampus(dto));
        Assertions.assertEquals("校区不存在", ex.getMsg());
    }

    private CampusVO buildVO(Long id, String name) {
        CampusVO vo = new CampusVO();
        vo.setId(id);
        vo.setName(name);
        vo.setSort(1);
        vo.setStatus(1);
        return vo;
    }
}
