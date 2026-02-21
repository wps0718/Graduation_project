package com.qingyuan.secondhand.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.MeetingPointDTO;
import com.qingyuan.secondhand.entity.MeetingPoint;
import com.qingyuan.secondhand.mapper.MeetingPointMapper;
import com.qingyuan.secondhand.vo.MeetingPointVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class MeetingPointServiceImplTest {

    @Test
    void testGetMiniListByCampusId_CacheHit() throws Exception {
        MeetingPointMapper meetingPointMapper = Mockito.mock(MeetingPointMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        String key = RedisConstant.MEETING_POINT_CAMPUS + 1L;
        String json = "[{\"id\":1,\"name\":\"图书馆门口\"}]";
        Mockito.when(valueOperations.get(key)).thenReturn(json);

        List<MeetingPointVO> cachedList = List.of(buildVO(1L, 1L, "图书馆门口"));
        Mockito.when(objectMapper.readValue(Mockito.eq(json), Mockito.any(TypeReference.class))).thenReturn(cachedList);

        MeetingPointServiceImpl service = new MeetingPointServiceImpl(meetingPointMapper, stringRedisTemplate, objectMapper);
        List<MeetingPointVO> result = service.getMiniListByCampusId(1L);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("图书馆门口", result.get(0).getName());
        Mockito.verifyNoInteractions(meetingPointMapper);
    }

    @Test
    void testGetMiniListByCampusId_CacheMiss() throws Exception {
        MeetingPointMapper meetingPointMapper = Mockito.mock(MeetingPointMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        String key = RedisConstant.MEETING_POINT_CAMPUS + 2L;
        Mockito.when(valueOperations.get(key)).thenReturn(null);

        MeetingPoint point = new MeetingPoint();
        point.setId(2L);
        point.setCampusId(2L);
        point.setName("食堂门口");
        point.setDescription("南门附近");
        point.setSort(1);
        point.setStatus(1);
        Mockito.when(meetingPointMapper.selectList(Mockito.any())).thenReturn(List.of(point));

        Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("[{\"id\":2,\"name\":\"食堂门口\"}]");

        MeetingPointServiceImpl service = new MeetingPointServiceImpl(meetingPointMapper, stringRedisTemplate, objectMapper);
        List<MeetingPointVO> result = service.getMiniListByCampusId(2L);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(2L, result.get(0).getId());

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TimeUnit> unitCaptor = ArgumentCaptor.forClass(TimeUnit.class);
        Mockito.verify(valueOperations).set(keyCaptor.capture(), jsonCaptor.capture(), ttlCaptor.capture(), unitCaptor.capture());

        Assertions.assertEquals(key, keyCaptor.getValue());
        Assertions.assertTrue(StringUtils.hasText(jsonCaptor.getValue()));
        Assertions.assertEquals(1L, ttlCaptor.getValue());
        Assertions.assertEquals(TimeUnit.HOURS, unitCaptor.getValue());
    }

    @Test
    void testGetAdminListByCampusId() {
        MeetingPointMapper meetingPointMapper = Mockito.mock(MeetingPointMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        MeetingPoint point = new MeetingPoint();
        point.setId(3L);
        point.setCampusId(3L);
        point.setName("北门");
        point.setSort(1);
        point.setStatus(1);
        Mockito.when(meetingPointMapper.selectList(Mockito.any())).thenReturn(List.of(point));

        MeetingPointServiceImpl service = new MeetingPointServiceImpl(meetingPointMapper, stringRedisTemplate, objectMapper);
        List<MeetingPointVO> result = service.getAdminListByCampusId(3L);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("北门", result.get(0).getName());
    }

    @Test
    void testAddMeetingPoint() {
        MeetingPointMapper meetingPointMapper = Mockito.mock(MeetingPointMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Mockito.when(meetingPointMapper.insert(Mockito.any(MeetingPoint.class))).thenReturn(1);

        MeetingPointServiceImpl service = new MeetingPointServiceImpl(meetingPointMapper, stringRedisTemplate, objectMapper);
        MeetingPointDTO dto = new MeetingPointDTO();
        dto.setCampusId(10L);
        dto.setName("操场");
        dto.setDescription("操场东侧");
        dto.setSort(1);
        dto.setStatus(1);
        service.addMeetingPoint(dto);

        Mockito.verify(meetingPointMapper).insert(Mockito.any(MeetingPoint.class));
        Mockito.verify(stringRedisTemplate).delete(RedisConstant.MEETING_POINT_CAMPUS + 10L);
    }

    @Test
    void testUpdateMeetingPoint_Success() {
        MeetingPointMapper meetingPointMapper = Mockito.mock(MeetingPointMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        MeetingPoint existing = new MeetingPoint();
        existing.setId(11L);
        existing.setCampusId(11L);
        Mockito.when(meetingPointMapper.selectById(11L)).thenReturn(existing);
        Mockito.when(meetingPointMapper.updateById(Mockito.any(MeetingPoint.class))).thenReturn(1);

        MeetingPointServiceImpl service = new MeetingPointServiceImpl(meetingPointMapper, stringRedisTemplate, objectMapper);
        MeetingPointDTO dto = new MeetingPointDTO();
        dto.setId(11L);
        dto.setCampusId(11L);
        dto.setName("篮球场");
        dto.setDescription("靠近教学楼");
        dto.setSort(1);
        dto.setStatus(1);
        service.updateMeetingPoint(dto);

        Mockito.verify(meetingPointMapper).updateById(Mockito.any(MeetingPoint.class));
        Mockito.verify(stringRedisTemplate).delete(RedisConstant.MEETING_POINT_CAMPUS + 11L);
    }

    @Test
    void testUpdateMeetingPoint_NotFound() {
        MeetingPointMapper meetingPointMapper = Mockito.mock(MeetingPointMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Mockito.when(meetingPointMapper.selectById(12L)).thenReturn(null);

        MeetingPointServiceImpl service = new MeetingPointServiceImpl(meetingPointMapper, stringRedisTemplate, objectMapper);
        MeetingPointDTO dto = new MeetingPointDTO();
        dto.setId(12L);
        dto.setCampusId(12L);
        dto.setName("篮球场");
        dto.setSort(1);
        dto.setStatus(1);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.updateMeetingPoint(dto));
        Assertions.assertEquals("面交地点不存在", ex.getMsg());
    }

    @Test
    void testDeleteMeetingPoint_Success() {
        MeetingPointMapper meetingPointMapper = Mockito.mock(MeetingPointMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        MeetingPoint existing = new MeetingPoint();
        existing.setId(13L);
        existing.setCampusId(13L);
        Mockito.when(meetingPointMapper.selectById(13L)).thenReturn(existing);
        Mockito.when(meetingPointMapper.deleteById(13L)).thenReturn(1);

        MeetingPointServiceImpl service = new MeetingPointServiceImpl(meetingPointMapper, stringRedisTemplate, objectMapper);
        service.deleteMeetingPoint(13L);

        Mockito.verify(meetingPointMapper).deleteById(13L);
        Mockito.verify(stringRedisTemplate).delete(RedisConstant.MEETING_POINT_CAMPUS + 13L);
    }

    @Test
    void testDeleteMeetingPoint_NotFound() {
        MeetingPointMapper meetingPointMapper = Mockito.mock(MeetingPointMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Mockito.when(meetingPointMapper.selectById(14L)).thenReturn(null);

        MeetingPointServiceImpl service = new MeetingPointServiceImpl(meetingPointMapper, stringRedisTemplate, objectMapper);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.deleteMeetingPoint(14L));
        Assertions.assertEquals("面交地点不存在", ex.getMsg());
    }

    private MeetingPointVO buildVO(Long id, Long campusId, String name) {
        MeetingPointVO vo = new MeetingPointVO();
        vo.setId(id);
        vo.setCampusId(campusId);
        vo.setName(name);
        vo.setSort(1);
        vo.setStatus(1);
        return vo;
    }
}
