package com.qingyuan.secondhand.controller.admin;

import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.CollegeDTO;
import com.qingyuan.secondhand.entity.College;
import com.qingyuan.secondhand.service.CollegeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/college")
@RequiredArgsConstructor
public class AdminCollegeController {

    private final CollegeService collegeService;

    @GetMapping("/list")
    public Result<List<College>> getCollegeList() {
        List<College> list = collegeService.list();
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result<Void> addCollege(@RequestBody @Valid CollegeDTO dto) {
        collegeService.addCollege(dto);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> updateCollege(@RequestBody @Valid CollegeDTO dto) {
        collegeService.updateCollege(dto);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteCollege(@RequestParam Long id) {
        collegeService.deleteCollege(id);
        return Result.success();
    }
}
