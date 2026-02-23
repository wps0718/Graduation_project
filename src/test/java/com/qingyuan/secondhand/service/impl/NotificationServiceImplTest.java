package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.enums.NotificationType;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.entity.Notification;
import com.qingyuan.secondhand.mapper.NotificationMapper;
import com.qingyuan.secondhand.vo.NotificationVO;
import com.qingyuan.secondhand.vo.UnreadCountVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @AfterEach
    void clearContext() {
        UserContext.removeCurrentUserId();
    }

    @Test
    void testGetNotificationList_Success() {
        NotificationMapper notificationMapper = Mockito.mock(NotificationMapper.class);
        NotificationServiceImpl service = new NotificationServiceImpl(notificationMapper);

        UserContext.setCurrentUserId(10001L);
        Page<Notification> pageResult = new Page<>(1, 10);
        pageResult.setTotal(3);
        pageResult.setRecords(List.of(
                buildNotification(1L, 10001L, 1, 0, 1),
                buildNotification(2L, 10001L, 2, 1, 2),
                buildNotification(3L, 10001L, 3, 0, 1)
        ));
        Mockito.when(notificationMapper.selectPage(Mockito.any(Page.class), Mockito.any()))
                .thenReturn(pageResult);

        IPage<NotificationVO> result = service.getNotificationList(1, 10, null);

        Assertions.assertEquals(3, result.getRecords().size());
        Assertions.assertEquals(1L, result.getRecords().get(0).getId());
    }

    @Test
    void testGetNotificationList_WithCategoryFilter() {
        NotificationMapper notificationMapper = Mockito.mock(NotificationMapper.class);
        NotificationServiceImpl service = new NotificationServiceImpl(notificationMapper);

        UserContext.setCurrentUserId(10001L);
        Page<Notification> pageResult = new Page<>(1, 10);
        pageResult.setTotal(1);
        pageResult.setRecords(List.of(buildNotification(1L, 10001L, 1, 0, 1)));
        Mockito.when(notificationMapper.selectPage(Mockito.any(Page.class), Mockito.any()))
                .thenReturn(pageResult);

        IPage<NotificationVO> result = service.getNotificationList(1, 10, 1);

        Assertions.assertEquals(1, result.getRecords().size());
        Assertions.assertEquals(1, result.getRecords().get(0).getCategory());
    }

    @Test
    void testMarkAsRead_Success() {
        NotificationMapper notificationMapper = Mockito.mock(NotificationMapper.class);
        NotificationServiceImpl service = new NotificationServiceImpl(notificationMapper);

        UserContext.setCurrentUserId(10001L);
        Notification notification = buildNotification(1L, 10001L, 1, 0, 1);
        Mockito.when(notificationMapper.selectById(1L)).thenReturn(notification);
        Mockito.when(notificationMapper.updateById(Mockito.any(Notification.class))).thenReturn(1);

        service.markAsRead(1L);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        Mockito.verify(notificationMapper).updateById(captor.capture());
        Assertions.assertEquals(1, captor.getValue().getIsRead());
    }

    @Test
    void testMarkAsRead_NotOwner() {
        NotificationMapper notificationMapper = Mockito.mock(NotificationMapper.class);
        NotificationServiceImpl service = new NotificationServiceImpl(notificationMapper);

        UserContext.setCurrentUserId(10001L);
        Notification notification = buildNotification(1L, 10002L, 1, 0, 1);
        Mockito.when(notificationMapper.selectById(1L)).thenReturn(notification);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.markAsRead(1L));
        Assertions.assertEquals("无权操作该通知", ex.getMsg());
    }

    @Test
    void testMarkAsRead_AlreadyRead() {
        NotificationMapper notificationMapper = Mockito.mock(NotificationMapper.class);
        NotificationServiceImpl service = new NotificationServiceImpl(notificationMapper);

        UserContext.setCurrentUserId(10001L);
        Notification notification = buildNotification(1L, 10001L, 1, 1, 1);
        Mockito.when(notificationMapper.selectById(1L)).thenReturn(notification);

        service.markAsRead(1L);

        Mockito.verify(notificationMapper, Mockito.never()).updateById(Mockito.any(Notification.class));
    }

    @Test
    void testMarkAllAsRead_Success() {
        NotificationMapper notificationMapper = Mockito.mock(NotificationMapper.class);
        NotificationServiceImpl service = new NotificationServiceImpl(notificationMapper);

        UserContext.setCurrentUserId(10001L);
        Mockito.when(notificationMapper.update(Mockito.isNull(), Mockito.any())).thenReturn(2);

        service.markAllAsRead();

        Mockito.verify(notificationMapper).update(Mockito.isNull(), Mockito.any());
    }

    @Test
    void testGetUnreadCount_Success() {
        NotificationMapper notificationMapper = Mockito.mock(NotificationMapper.class);
        NotificationServiceImpl service = new NotificationServiceImpl(notificationMapper);

        UserContext.setCurrentUserId(10001L);
        Mockito.when(notificationMapper.selectCount(Mockito.any())).thenReturn(5L, 2L, 3L);

        UnreadCountVO count = service.getUnreadCount();

        Assertions.assertEquals(5L, count.getTotal());
        Assertions.assertEquals(2L, count.getTrade());
        Assertions.assertEquals(3L, count.getSystem());
    }

    @Test
    void testSend_Success() {
        NotificationMapper notificationMapper = Mockito.mock(NotificationMapper.class);
        NotificationServiceImpl service = new NotificationServiceImpl(notificationMapper);

        Mockito.when(notificationMapper.insert(Mockito.any(Notification.class))).thenReturn(1);

        service.send(10001L, 1, "测试标题", "测试内容", 100L, 2, 1);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        Mockito.verify(notificationMapper).insert(captor.capture());
        Notification saved = captor.getValue();
        Assertions.assertEquals(10001L, saved.getUserId());
        Assertions.assertEquals(1, saved.getType());
        Assertions.assertEquals("测试标题", saved.getTitle());
        Assertions.assertEquals("测试内容", saved.getContent());
        Assertions.assertEquals(100L, saved.getRelatedId());
        Assertions.assertEquals(2, saved.getRelatedType());
        Assertions.assertEquals(0, saved.getIsRead());
        Assertions.assertEquals(1, saved.getCategory());
    }

    @Test
    void testNotificationTypeFormatContent() {
        String content = NotificationType.TRADE_SUCCESS.formatContent(Map.of("productName", "相机"));

        Assertions.assertEquals("你购买的「相机」交易已完成，给卖家一个评价吧！", content);
    }

    @Test
    void testSendWithTemplate_Success() {
        NotificationMapper notificationMapper = Mockito.mock(NotificationMapper.class);
        NotificationServiceImpl service = new NotificationServiceImpl(notificationMapper);

        Mockito.when(notificationMapper.insert(Mockito.any(Notification.class))).thenReturn(1);

        service.send(10001L, NotificationType.TRADE_SUCCESS, Map.of("productName", "耳机"), 100L, 2, 1);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        Mockito.verify(notificationMapper).insert(captor.capture());
        Notification saved = captor.getValue();
        Assertions.assertEquals(10001L, saved.getUserId());
        Assertions.assertEquals(1, saved.getType());
        Assertions.assertEquals("交易成功", saved.getTitle());
        Assertions.assertEquals("你购买的「耳机」交易已完成，给卖家一个评价吧！", saved.getContent());
        Assertions.assertEquals(100L, saved.getRelatedId());
        Assertions.assertEquals(2, saved.getRelatedType());
        Assertions.assertEquals(1, saved.getCategory());
    }

    private Notification buildNotification(Long id, Long userId, Integer type, Integer isRead, Integer category) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle("标题");
        notification.setContent("内容");
        notification.setRelatedId(10L);
        notification.setRelatedType(1);
        notification.setIsRead(isRead);
        notification.setCategory(category);
        notification.setCreateTime(LocalDateTime.now());
        return notification;
    }
}
