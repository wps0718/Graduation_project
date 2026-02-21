package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.dto.NoticeDTO;
import com.qingyuan.secondhand.entity.Notice;
import com.qingyuan.secondhand.vo.NoticeVO;

public interface NoticeService extends IService<Notice> {
    Page<NoticeVO> getNoticePage(Integer page, Integer pageSize, Integer type, Integer status);

    void addNotice(NoticeDTO dto);

    void updateNotice(NoticeDTO dto);

    void deleteNotice(Long id);
}
