package com.qingyuan.secondhand.controller.mini;

import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.service.CollegeService;
import com.qingyuan.secondhand.vo.CollegeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mini/college")
@RequiredArgsConstructor
public class MiniCollegeController {

    private final CollegeService collegeService;

    @GetMapping("/list")
    public Result<List<CollegeVO>> getCollegeList() {
        List<CollegeVO> list = collegeService.getCollegeList();
        return Result.success(list);
    }
}
