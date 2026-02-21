package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.NoticeDTO;
import com.qingyuan.secondhand.entity.Notice;
import com.qingyuan.secondhand.mapper.NoticeMapper;
import com.qingyuan.secondhand.service.NoticeService;
import com.qingyuan.secondhand.vo.NoticeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    private final NoticeMapper noticeMapper;
    private final NoticeAsyncService noticeAsyncService;

    @Override
    public Page<NoticeVO> getNoticePage(Integer page, Integer pageSize, Integer type, Integer status) {
        Page<Notice> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(Notice::getType, type);
        }
        if (status != null) {
            wrapper.eq(Notice::getStatus, status);
        }
        wrapper.orderByDesc(Notice::getCreateTime);
        Page<Notice> result = noticeMapper.selectPage(pageObj, wrapper);
        Page<NoticeVO> voPage = new Page<>(result.getCurrent(), result.getSize());
        voPage.setTotal(result.getTotal());
        List<NoticeVO> records = result.getRecords() == null ? List.of() : result.getRecords().stream().map(this::toNoticeVO).toList();
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public void addNotice(NoticeDTO dto) {
        Long adminId = UserContext.getCurrentUserId();
        if (adminId == null) {
            throw new BusinessException("未登录");
        }
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setType(dto.getType());
        notice.setStatus(dto.getStatus());
        notice.setPublisherId(adminId);
        int inserted = noticeMapper.insert(notice);
        if (inserted <= 0) {
            throw new BusinessException("新增公告失败");
        }
        if (Integer.valueOf(1).equals(dto.getStatus())) {
            noticeAsyncService.pushNoticeToAllUsers(notice.getId(), notice.getTitle(), notice.getContent());
        }
    }

    @Override
    public void updateNotice(NoticeDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("公告ID不能为空");
        }
        Notice existing = noticeMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException("公告不存在");
        }
        Notice notice = new Notice();
        notice.setId(dto.getId());
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setType(dto.getType());
        notice.setStatus(dto.getStatus());
        int updated = noticeMapper.updateById(notice);
        if (updated <= 0) {
            throw new BusinessException("更新公告失败");
        }
    }

    @Override
    public void deleteNotice(Long id) {
        if (id == null) {
            throw new BusinessException("公告ID不能为空");
        }
        Notice existing = noticeMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("公告不存在");
        }
        int deleted = noticeMapper.deleteById(id);
        if (deleted <= 0) {
            throw new BusinessException("删除公告失败");
        }
    }

    private NoticeVO toNoticeVO(Notice notice) {
        NoticeVO vo = new NoticeVO();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        vo.setContent(notice.getContent());
        vo.setType(notice.getType());
        vo.setStatus(notice.getStatus());
        vo.setPublisherId(notice.getPublisherId());
        vo.setCreateTime(notice.getCreateTime());
        vo.setUpdateTime(notice.getUpdateTime());
        return vo;
    }
}
