package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.CampusDTO;
import com.qingyuan.secondhand.entity.Campus;
import com.qingyuan.secondhand.mapper.CampusMapper;
import com.qingyuan.secondhand.service.CampusService;
import com.qingyuan.secondhand.vo.CampusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CampusServiceImpl extends ServiceImpl<CampusMapper, Campus> implements CampusService {

    private final CampusMapper campusMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<CampusVO> getMiniList() {
        String cacheKey = RedisConstant.CAMPUS_LIST;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<CampusVO>>() {
                });
            } catch (Exception e) {
                stringRedisTemplate.delete(cacheKey);
            }
        }

        List<Campus> list = campusMapper.selectList(new LambdaQueryWrapper<Campus>()
                .eq(Campus::getStatus, 1)
                .orderByAsc(Campus::getSort));
        List<CampusVO> result = list == null ? List.of() : list.stream().map(this::toCampusVO).toList();

        try {
            String json = objectMapper.writeValueAsString(result);
            stringRedisTemplate.opsForValue().set(cacheKey, json, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            return result;
        }

        return result;
    }

    @Override
    public List<CampusVO> getAdminList() {
        List<Campus> list = campusMapper.selectList(new LambdaQueryWrapper<Campus>()
                .orderByAsc(Campus::getSort));
        return list == null ? List.of() : list.stream().map(this::toCampusVO).toList();
    }

    @Override
    public void addCampus(CampusDTO dto) {
        Campus campus = new Campus();
        campus.setName(dto.getName());
        campus.setAddress(dto.getAddress());
        campus.setSort(dto.getSort());
        campus.setStatus(dto.getStatus());
        int inserted = campusMapper.insert(campus);
        if (inserted <= 0) {
            throw new BusinessException("新增校区失败");
        }
        stringRedisTemplate.delete(RedisConstant.CAMPUS_LIST);
    }

    @Override
    public void updateCampus(CampusDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("校区ID不能为空");
        }
        Campus existing = campusMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException("校区不存在");
        }
        Campus campus = new Campus();
        campus.setId(dto.getId());
        campus.setName(dto.getName());
        campus.setAddress(dto.getAddress());
        campus.setSort(dto.getSort());
        campus.setStatus(dto.getStatus());
        campus.setUpdateTime(LocalDateTime.now());
        int updated = campusMapper.updateById(campus);
        if (updated <= 0) {
            throw new BusinessException("更新校区失败");
        }
        stringRedisTemplate.delete(RedisConstant.CAMPUS_LIST);
    }

    private CampusVO toCampusVO(Campus campus) {
        CampusVO vo = new CampusVO();
        vo.setId(campus.getId());
        vo.setName(campus.getName());
        vo.setAddress(campus.getAddress());
        vo.setSort(campus.getSort());
        vo.setStatus(campus.getStatus());
        return vo;
    }
}
