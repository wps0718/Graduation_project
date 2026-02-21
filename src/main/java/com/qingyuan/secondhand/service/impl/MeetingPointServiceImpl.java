package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.MeetingPointDTO;
import com.qingyuan.secondhand.entity.MeetingPoint;
import com.qingyuan.secondhand.mapper.MeetingPointMapper;
import com.qingyuan.secondhand.service.MeetingPointService;
import com.qingyuan.secondhand.vo.MeetingPointVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MeetingPointServiceImpl extends ServiceImpl<MeetingPointMapper, MeetingPoint> implements MeetingPointService {

    private final MeetingPointMapper meetingPointMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<MeetingPointVO> getMiniListByCampusId(Long campusId) {
        String cacheKey = RedisConstant.MEETING_POINT_CAMPUS + campusId;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<MeetingPointVO>>() {
                });
            } catch (Exception e) {
                stringRedisTemplate.delete(cacheKey);
            }
        }

        List<MeetingPoint> list = meetingPointMapper.selectList(new LambdaQueryWrapper<MeetingPoint>()
                .eq(MeetingPoint::getCampusId, campusId)
                .eq(MeetingPoint::getStatus, 1)
                .orderByAsc(MeetingPoint::getSort));
        List<MeetingPointVO> result = list == null ? List.of() : list.stream().map(this::toMeetingPointVO).toList();

        try {
            String json = objectMapper.writeValueAsString(result);
            stringRedisTemplate.opsForValue().set(cacheKey, json, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            return result;
        }

        return result;
    }

    @Override
    public List<MeetingPointVO> getAdminListByCampusId(Long campusId) {
        List<MeetingPoint> list = meetingPointMapper.selectList(new LambdaQueryWrapper<MeetingPoint>()
                .eq(MeetingPoint::getCampusId, campusId)
                .orderByAsc(MeetingPoint::getSort));
        return list == null ? List.of() : list.stream().map(this::toMeetingPointVO).toList();
    }

    @Override
    public void addMeetingPoint(MeetingPointDTO dto) {
        MeetingPoint meetingPoint = new MeetingPoint();
        meetingPoint.setCampusId(dto.getCampusId());
        meetingPoint.setName(dto.getName());
        meetingPoint.setDescription(dto.getDescription());
        meetingPoint.setSort(dto.getSort());
        meetingPoint.setStatus(dto.getStatus());
        int inserted = meetingPointMapper.insert(meetingPoint);
        if (inserted <= 0) {
            throw new BusinessException("新增面交地点失败");
        }
        stringRedisTemplate.delete(RedisConstant.MEETING_POINT_CAMPUS + dto.getCampusId());
    }

    @Override
    public void updateMeetingPoint(MeetingPointDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("面交地点ID不能为空");
        }
        MeetingPoint existing = meetingPointMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException("面交地点不存在");
        }
        MeetingPoint meetingPoint = new MeetingPoint();
        meetingPoint.setId(dto.getId());
        meetingPoint.setCampusId(dto.getCampusId());
        meetingPoint.setName(dto.getName());
        meetingPoint.setDescription(dto.getDescription());
        meetingPoint.setSort(dto.getSort());
        meetingPoint.setStatus(dto.getStatus());
        meetingPoint.setUpdateTime(LocalDateTime.now());
        int updated = meetingPointMapper.updateById(meetingPoint);
        if (updated <= 0) {
            throw new BusinessException("更新面交地点失败");
        }
        stringRedisTemplate.delete(RedisConstant.MEETING_POINT_CAMPUS + meetingPoint.getCampusId());
    }

    @Override
    public void deleteMeetingPoint(Long id) {
        if (id == null) {
            throw new BusinessException("面交地点ID不能为空");
        }
        MeetingPoint existing = meetingPointMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("面交地点不存在");
        }
        int deleted = meetingPointMapper.deleteById(id);
        if (deleted <= 0) {
            throw new BusinessException("删除面交地点失败");
        }
        stringRedisTemplate.delete(RedisConstant.MEETING_POINT_CAMPUS + existing.getCampusId());
    }

    private MeetingPointVO toMeetingPointVO(MeetingPoint meetingPoint) {
        MeetingPointVO vo = new MeetingPointVO();
        vo.setId(meetingPoint.getId());
        vo.setCampusId(meetingPoint.getCampusId());
        vo.setName(meetingPoint.getName());
        vo.setDescription(meetingPoint.getDescription());
        vo.setSort(meetingPoint.getSort());
        vo.setStatus(meetingPoint.getStatus());
        return vo;
    }
}
