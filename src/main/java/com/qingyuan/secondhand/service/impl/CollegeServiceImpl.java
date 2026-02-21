package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.CollegeDTO;
import com.qingyuan.secondhand.entity.College;
import com.qingyuan.secondhand.entity.CampusAuth;
import com.qingyuan.secondhand.mapper.CampusAuthMapper;
import com.qingyuan.secondhand.mapper.CollegeMapper;
import com.qingyuan.secondhand.service.CollegeService;
import com.qingyuan.secondhand.vo.CollegeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {

    private final CollegeMapper collegeMapper;
    private final CampusAuthMapper campusAuthMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<CollegeVO> getCollegeList() {
        String cacheKey = RedisConstant.COLLEGE_LIST;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<CollegeVO>>() {
                });
            } catch (Exception e) {
                stringRedisTemplate.delete(cacheKey);
            }
        }

        List<College> list = collegeMapper.selectList(new LambdaQueryWrapper<College>()
                .eq(College::getStatus, 1)
                .orderByAsc(College::getSort));

        List<CollegeVO> result = list == null ? List.of() : list.stream().map(this::toCollegeVO).toList();

        try {
            String json = objectMapper.writeValueAsString(result);
            stringRedisTemplate.opsForValue().set(cacheKey, json, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            return result;
        }

        return result;
    }

    @Override
    public void addCollege(CollegeDTO dto) {
        College college = new College();
        college.setName(dto.getName());
        college.setSort(dto.getSort());
        college.setStatus(dto.getStatus());
        int inserted = collegeMapper.insert(college);
        if (inserted <= 0) {
            throw new BusinessException("新增学院失败");
        }
        stringRedisTemplate.delete(RedisConstant.COLLEGE_LIST);
    }

    @Override
    public void updateCollege(CollegeDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("学院ID不能为空");
        }
        College existing = collegeMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException("学院不存在");
        }
        College college = new College();
        college.setId(dto.getId());
        college.setName(dto.getName());
        college.setSort(dto.getSort());
        college.setStatus(dto.getStatus());
        college.setUpdateTime(LocalDateTime.now());
        int updated = collegeMapper.updateById(college);
        if (updated <= 0) {
            throw new BusinessException("更新学院失败");
        }
        stringRedisTemplate.delete(RedisConstant.COLLEGE_LIST);
    }

    @Override
    public void deleteCollege(Long id) {
        if (id == null) {
            throw new BusinessException("学院ID不能为空");
        }
        College existing = collegeMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("学院不存在");
        }
        Long count = campusAuthMapper.selectCount(new LambdaQueryWrapper<CampusAuth>()
                .eq(CampusAuth::getCollegeId, id));
        if (count != null && count > 0) {
            throw new BusinessException("该学院下存在认证记录，无法删除");
        }
        int deleted = collegeMapper.deleteById(id);
        if (deleted <= 0) {
            throw new BusinessException("删除学院失败");
        }
        stringRedisTemplate.delete(RedisConstant.COLLEGE_LIST);
    }

    private CollegeVO toCollegeVO(College college) {
        CollegeVO vo = new CollegeVO();
        vo.setId(college.getId());
        vo.setName(college.getName());
        return vo;
    }
}
