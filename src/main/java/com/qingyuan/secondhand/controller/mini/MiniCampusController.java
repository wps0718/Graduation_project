package com.qingyuan.secondhand.controller.mini;

import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.service.CampusService;
import com.qingyuan.secondhand.service.MeetingPointService;
import com.qingyuan.secondhand.vo.CampusVO;
import com.qingyuan.secondhand.vo.MeetingPointVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mini/campus")
@RequiredArgsConstructor
public class MiniCampusController {

    private final CampusService campusService;
    private final MeetingPointService meetingPointService;

    @GetMapping("/list")
    public Result<List<CampusVO>> getCampusList() {
        List<CampusVO> list = campusService.getMiniList();
        return Result.success(list);
    }

    @GetMapping("/meeting-points/{campusId}")
    public Result<List<MeetingPointVO>> getMeetingPoints(@PathVariable Long campusId) {
        List<MeetingPointVO> list = meetingPointService.getMiniListByCampusId(campusId);
        return Result.success(list);
    }
}
