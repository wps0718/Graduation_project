package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.dto.AuthSubmitDTO;
import com.qingyuan.secondhand.entity.CampusAuth;
import com.qingyuan.secondhand.vo.AuthPageVO;
import com.qingyuan.secondhand.vo.AuthHistoryVO;
import com.qingyuan.secondhand.vo.AuthStatusVO;

import java.util.List;

public interface CampusAuthService extends IService<CampusAuth> {
    void submitAuth(AuthSubmitDTO dto);

    AuthStatusVO getAuthStatus();

    List<AuthHistoryVO> listAuthHistory();

    AuthHistoryVO getAuthHistoryDetail(Long id);

    List<AuthHistoryVO> listAuthHistoryByAuthId(Long authId);

    Page<AuthPageVO> pageAuth(Integer page, Integer size, Integer status, Long collegeId);

    AuthPageVO getAuthDetail(Long id);

    void approveAuth(Long id);

    void rejectAuth(Long id, String rejectReason);
}
