package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.entity.Notification;
import com.qingyuan.secondhand.vo.NotificationVO;

public interface NotificationService extends IService<Notification> {
    IPage<NotificationVO> getNotificationList(Integer page, Integer pageSize, Integer category);

    void markAsRead(Long notificationId);

    void markAllAsRead();

    Long getUnreadCount();

    void send(Long userId, Integer type, String title, String content, Long relatedId, Integer relatedType, Integer category);

    void sendNotification(Long userId, Integer type, String content);
}
