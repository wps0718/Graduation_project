package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.dto.CollegeDTO;
import com.qingyuan.secondhand.entity.College;
import com.qingyuan.secondhand.vo.CollegeVO;

import java.util.List;

public interface CollegeService extends IService<College> {
    List<CollegeVO> getCollegeList();

    void addCollege(CollegeDTO dto);

    void updateCollege(CollegeDTO dto);

    void deleteCollege(Long id);
}
