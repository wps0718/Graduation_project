package com.qingyuan.secondhand.controller.admin;

import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.CampusDTO;
import com.qingyuan.secondhand.dto.MeetingPointDTO;
import com.qingyuan.secondhand.service.CampusService;
import com.qingyuan.secondhand.service.MeetingPointService;
import com.qingyuan.secondhand.vo.CampusVO;
import com.qingyuan.secondhand.vo.MeetingPointVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/campus")
@RequiredArgsConstructor
public class AdminCampusController {

    private final CampusService campusService;
    private final MeetingPointService meetingPointService;

    @GetMapping("/list")
    public Result<List<CampusVO>> getCampusList() {
        List<CampusVO> list = campusService.getAdminList();
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result<Void> addCampus(@RequestBody @Valid CampusDTO dto) {
        campusService.addCampus(dto);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> updateCampus(@RequestBody @Valid CampusDTO dto) {
        campusService.updateCampus(dto);
        return Result.success();
    }

    @GetMapping("/meeting-point/list/{campusId}")
    public Result<List<MeetingPointVO>> getMeetingPointList(@PathVariable Long campusId) {
        List<MeetingPointVO> list = meetingPointService.getAdminListByCampusId(campusId);
        return Result.success(list);
    }

    @PostMapping("/meeting-point/add")
    public Result<Void> addMeetingPoint(@RequestBody @Valid MeetingPointDTO dto) {
        meetingPointService.addMeetingPoint(dto);
        return Result.success();
    }

    @PostMapping("/meeting-point/update")
    public Result<Void> updateMeetingPoint(@RequestBody @Valid MeetingPointDTO dto) {
        meetingPointService.updateMeetingPoint(dto);
        return Result.success();
    }

    @PostMapping("/meeting-point/delete")
    public Result<Void> deleteMeetingPoint(@RequestParam Long id) {
        meetingPointService.deleteMeetingPoint(id);
        return Result.success();
    }
}
