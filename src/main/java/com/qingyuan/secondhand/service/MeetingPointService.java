package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.dto.MeetingPointDTO;
import com.qingyuan.secondhand.entity.MeetingPoint;
import com.qingyuan.secondhand.vo.MeetingPointVO;

import java.util.List;

public interface MeetingPointService extends IService<MeetingPoint> {
    List<MeetingPointVO> getMiniListByCampusId(Long campusId);

    List<MeetingPointVO> getAdminListByCampusId(Long campusId);

    void addMeetingPoint(MeetingPointDTO dto);

    void updateMeetingPoint(MeetingPointDTO dto);

    void deleteMeetingPoint(Long id);
}
