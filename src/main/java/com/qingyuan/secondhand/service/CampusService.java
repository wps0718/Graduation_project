package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.dto.CampusDTO;
import com.qingyuan.secondhand.entity.Campus;
import com.qingyuan.secondhand.vo.CampusVO;

import java.util.List;

public interface CampusService extends IService<Campus> {
    List<CampusVO> getMiniList();

    List<CampusVO> getAdminList();

    void addCampus(CampusDTO dto);

    void updateCampus(CampusDTO dto);
}
