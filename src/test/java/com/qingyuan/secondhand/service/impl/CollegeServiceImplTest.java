package com.qingyuan.secondhand.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.CollegeDTO;
import com.qingyuan.secondhand.entity.College;
import com.qingyuan.secondhand.mapper.CampusAuthMapper;
import com.qingyuan.secondhand.mapper.CollegeMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class CollegeServiceImplTest {

    @Test
    void testAddCollege_Success() {
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Mockito.when(collegeMapper.insert(Mockito.any(College.class))).thenReturn(1);

        CollegeServiceImpl service = new CollegeServiceImpl(collegeMapper, campusAuthMapper, stringRedisTemplate, objectMapper);
        CollegeDTO dto = new CollegeDTO();
        dto.setName("信息学院");
        dto.setSort(1);
        dto.setStatus(1);
        service.addCollege(dto);

        Mockito.verify(collegeMapper).insert(Mockito.any(College.class));
        Mockito.verify(stringRedisTemplate).delete(RedisConstant.COLLEGE_LIST);
    }

    @Test
    void testUpdateCollege_Success() {
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        College existing = new College();
        existing.setId(10L);
        Mockito.when(collegeMapper.selectById(10L)).thenReturn(existing);
        Mockito.when(collegeMapper.updateById(Mockito.any(College.class))).thenReturn(1);

        CollegeServiceImpl service = new CollegeServiceImpl(collegeMapper, campusAuthMapper, stringRedisTemplate, objectMapper);
        CollegeDTO dto = new CollegeDTO();
        dto.setId(10L);
        dto.setName("电气学院");
        dto.setSort(2);
        dto.setStatus(1);
        service.updateCollege(dto);

        Mockito.verify(collegeMapper).updateById(Mockito.any(College.class));
        Mockito.verify(stringRedisTemplate).delete(RedisConstant.COLLEGE_LIST);
    }

    @Test
    void testUpdateCollege_NotFound() {
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Mockito.when(collegeMapper.selectById(11L)).thenReturn(null);

        CollegeServiceImpl service = new CollegeServiceImpl(collegeMapper, campusAuthMapper, stringRedisTemplate, objectMapper);
        CollegeDTO dto = new CollegeDTO();
        dto.setId(11L);
        dto.setName("机械学院");
        dto.setSort(1);
        dto.setStatus(1);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.updateCollege(dto));
        Assertions.assertEquals("学院不存在", ex.getMsg());
    }

    @Test
    void testDeleteCollege_Success() {
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        College existing = new College();
        existing.setId(1L);
        Mockito.when(collegeMapper.selectById(1L)).thenReturn(existing);
        Mockito.when(campusAuthMapper.selectCount(Mockito.any())).thenReturn(0L);
        Mockito.when(collegeMapper.deleteById(1L)).thenReturn(1);

        CollegeServiceImpl service = new CollegeServiceImpl(collegeMapper, campusAuthMapper, stringRedisTemplate, objectMapper);
        service.deleteCollege(1L);

        Mockito.verify(collegeMapper).deleteById(1L);
        Mockito.verify(stringRedisTemplate).delete(RedisConstant.COLLEGE_LIST);
    }

    @Test
    void testDeleteCollege_HasAuthRecords() {
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        College existing = new College();
        existing.setId(2L);
        Mockito.when(collegeMapper.selectById(2L)).thenReturn(existing);
        Mockito.when(campusAuthMapper.selectCount(Mockito.any())).thenReturn(1L);

        CollegeServiceImpl service = new CollegeServiceImpl(collegeMapper, campusAuthMapper, stringRedisTemplate, objectMapper);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.deleteCollege(2L));
        Assertions.assertEquals("该学院下存在认证记录，无法删除", ex.getMsg());
        Mockito.verify(collegeMapper, Mockito.never()).deleteById(Mockito.anyLong());
    }

    @Test
    void testDeleteCollege_NotFound() {
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Mockito.when(collegeMapper.selectById(3L)).thenReturn(null);

        CollegeServiceImpl service = new CollegeServiceImpl(collegeMapper, campusAuthMapper, stringRedisTemplate, objectMapper);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.deleteCollege(3L));
        Assertions.assertEquals("学院不存在", ex.getMsg());
    }
}
