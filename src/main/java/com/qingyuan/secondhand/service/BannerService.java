package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.dto.BannerDTO;
import com.qingyuan.secondhand.entity.Banner;
import com.qingyuan.secondhand.vo.BannerVO;

import java.util.List;

public interface BannerService extends IService<Banner> {
    List<BannerVO> getMiniBannerList(Long campusId);

    IPage<BannerVO> getAdminBannerPage(Integer page, Integer pageSize, Integer status, Long campusId);

    void addBanner(BannerDTO dto);

    void updateBanner(Long id, BannerDTO dto);

    void deleteBanner(Long id);
}
