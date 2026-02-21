package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingyuan.secondhand.common.enums.NotificationCategory;
import com.qingyuan.secondhand.common.enums.UserStatus;
import com.qingyuan.secondhand.entity.Notification;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.NotificationMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeAsyncService {

    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;

    @Async
    public void pushNoticeToAllUsers(Long noticeId, String title, String content) {
        if (noticeId == null) {
            return;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStatus, UserStatus.NORMAL.getCode());
        List<User> users = userMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(users)) {
            return;
        }
        int batchSize = 1000;
        for (int i = 0; i < users.size(); i += batchSize) {
            int end = Math.min(i + batchSize, users.size());
            List<User> batch = users.subList(i, end);
            List<Notification> notifications = new ArrayList<>(batch.size());
            LocalDateTime now = LocalDateTime.now();
            for (User user : batch) {
                Notification notification = new Notification();
                notification.setUserId(user.getId());
                notification.setType(5);
                notification.setTitle(title);
                notification.setContent(content);
                notification.setRelatedId(noticeId);
                notification.setRelatedType(3);
                notification.setCategory(NotificationCategory.SYSTEM.getCode());
                notification.setIsRead(0);
                notification.setCreateTime(now);
                notification.setUpdateTime(now);
                notifications.add(notification);
            }
            notificationMapper.insertBatch(notifications);
        }
    }
}
