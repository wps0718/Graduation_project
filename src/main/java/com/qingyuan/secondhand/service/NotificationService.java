package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.common.enums.NotificationType;
import com.qingyuan.secondhand.entity.Notification;
import com.qingyuan.secondhand.vo.FavoriteNotificationVO;
import com.qingyuan.secondhand.vo.FollowerNotificationVO;
import com.qingyuan.secondhand.vo.NotificationVO;
import com.qingyuan.secondhand.vo.UnreadCountVO;

import java.util.Map;

public interface NotificationService extends IService<Notification> {
    IPage<NotificationVO> getNotificationList(Integer page, Integer pageSize, Integer category);

    IPage<FavoriteNotificationVO> getFavoriteNotificationList(Integer page, Integer pageSize);

    IPage<FollowerNotificationVO> getFollowerNotificationList(Integer page, Integer pageSize);

    void markAsRead(Long notificationId);

    void markBatchAsRead(java.util.List<Long> notificationIds);

    void markTypeAsRead(Integer type);

    void markAllAsRead();

    UnreadCountVO getUnreadCount();

    void send(Long userId, NotificationType type, Map<String, String> params, Long relatedId, Integer relatedType, Integer category);

    void send(Long userId, Integer type, String title, String content, Long relatedId, Integer relatedType, Integer category);

    void sendNotification(Long userId, Integer type, String content);
}
