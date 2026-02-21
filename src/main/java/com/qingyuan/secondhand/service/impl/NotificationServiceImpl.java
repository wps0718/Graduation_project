package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.entity.Notification;
import com.qingyuan.secondhand.mapper.NotificationMapper;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public IPage<NotificationVO> getNotificationList(Integer page, Integer pageSize, Integer category) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (page == null || pageSize == null) {
            throw new BusinessException("分页参数不能为空");
        }
        if (category != null && category != 1 && category != 2) {
            throw new BusinessException("消息分类不正确");
        }
        Page<Notification> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId);
        if (category != null) {
            wrapper.eq(Notification::getCategory, category);
        }
        wrapper.orderByDesc(Notification::getCreateTime);
        IPage<Notification> result = notificationMapper.selectPage(pageObj, wrapper);
        if (result == null) {
            return new Page<>(page, pageSize, 0);
        }
        return result.convert(this::toNotificationVO);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (notificationId == null) {
            throw new BusinessException("通知ID不能为空");
        }
        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException("通知不存在");
        }
        if (!userId.equals(notification.getUserId())) {
            throw new BusinessException("无权操作该通知");
        }
        if (Integer.valueOf(1).equals(notification.getIsRead())) {
            return;
        }
        Notification update = new Notification();
        update.setId(notificationId);
        update.setIsRead(1);
        int updated = notificationMapper.updateById(update);
        if (updated <= 0) {
            throw new BusinessException("标记已读失败");
        }
    }

    @Override
    public void markAllAsRead() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        UpdateWrapper<Notification> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("is_read", 0)
                .set("is_read", 1);
        notificationMapper.update(null, wrapper);
    }

    @Override
    public Long getUnreadCount() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Long count = notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
        return count == null ? 0L : count;
    }

    @Override
    @Async
    public void send(Long userId, Integer type, String title, String content, Long relatedId, Integer relatedType, Integer category) {
        validateSend(userId, type, title, content, relatedType, category);
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setIsRead(0);
        notification.setCategory(category);
        int inserted = notificationMapper.insert(notification);
        if (inserted <= 0) {
            throw new BusinessException("发送通知失败");
        }
    }

    @Override
    @Async
    public void sendNotification(Long userId, Integer type, String content) {
        send(userId, type, content, content, null, null, 1);
    }

    private NotificationVO toNotificationVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setType(notification.getType());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setRelatedId(notification.getRelatedId());
        vo.setRelatedType(notification.getRelatedType());
        vo.setIsRead(notification.getIsRead());
        vo.setCategory(notification.getCategory());
        vo.setCreateTime(notification.getCreateTime());
        return vo;
    }

    private void validateSend(Long userId, Integer type, String title, String content, Integer relatedType, Integer category) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (type == null) {
            throw new BusinessException("消息类型不能为空");
        }
        if (type < 1 || type > 10) {
            throw new BusinessException("消息类型不正确");
        }
        if (!StringUtils.hasText(title)) {
            throw new BusinessException("消息标题不能为空");
        }
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("消息内容不能为空");
        }
        if (category == null || (category != 1 && category != 2)) {
            throw new BusinessException("消息分类不正确");
        }
        if (relatedType != null && (relatedType < 1 || relatedType > 4)) {
            throw new BusinessException("关联类型不正确");
        }
    }
}
