package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.enums.UserStatus;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.NoticeDTO;
import com.qingyuan.secondhand.entity.Notice;
import com.qingyuan.secondhand.entity.Notification;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.NoticeMapper;
import com.qingyuan.secondhand.mapper.NotificationMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.vo.NoticeVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class NoticeServiceImplTest {

    @AfterEach
    void clearContext() {
        UserContext.removeCurrentUserId();
    }

    @Test
    void testGetNoticePage_Success() {
        NoticeMapper noticeMapper = Mockito.mock(NoticeMapper.class);
        NoticeAsyncService noticeAsyncService = Mockito.mock(NoticeAsyncService.class);
        NoticeServiceImpl service = new NoticeServiceImpl(noticeMapper, noticeAsyncService);

        Page<Notice> page = new Page<>(1, 10);
        Notice notice = buildNotice(1L, "标题", "内容", 1, 1);
        page.setRecords(List.of(notice));
        page.setTotal(1);
        Mockito.when(noticeMapper.selectPage(Mockito.any(Page.class), Mockito.any())).thenReturn(page);

        Page<NoticeVO> result = service.getNoticePage(1, 10, null, null);

        Assertions.assertEquals(1, result.getTotal());
        Assertions.assertEquals("标题", result.getRecords().get(0).getTitle());
        Mockito.verify(noticeMapper).selectPage(Mockito.any(Page.class), Mockito.any());
    }

    @Test
    void testGetNoticePage_WithTypeFilter() {
        NoticeMapper noticeMapper = Mockito.mock(NoticeMapper.class);
        NoticeAsyncService noticeAsyncService = Mockito.mock(NoticeAsyncService.class);
        NoticeServiceImpl service = new NoticeServiceImpl(noticeMapper, noticeAsyncService);

        Page<Notice> page = new Page<>(1, 10);
        page.setRecords(List.of(buildNotice(2L, "标题2", "内容2", 2, 1)));
        page.setTotal(1);
        Mockito.when(noticeMapper.selectPage(Mockito.any(Page.class), Mockito.any())).thenReturn(page);

        Page<NoticeVO> result = service.getNoticePage(1, 10, 2, null);

        Assertions.assertEquals(1, result.getTotal());
        Assertions.assertEquals(2, result.getRecords().get(0).getType());
    }

    @Test
    void testGetNoticePage_WithStatusFilter() {
        NoticeMapper noticeMapper = Mockito.mock(NoticeMapper.class);
        NoticeAsyncService noticeAsyncService = Mockito.mock(NoticeAsyncService.class);
        NoticeServiceImpl service = new NoticeServiceImpl(noticeMapper, noticeAsyncService);

        Page<Notice> page = new Page<>(1, 10);
        page.setRecords(List.of(buildNotice(3L, "标题3", "内容3", 1, 0)));
        page.setTotal(1);
        Mockito.when(noticeMapper.selectPage(Mockito.any(Page.class), Mockito.any())).thenReturn(page);

        Page<NoticeVO> result = service.getNoticePage(1, 10, null, 0);

        Assertions.assertEquals(1, result.getTotal());
        Assertions.assertEquals(0, result.getRecords().get(0).getStatus());
    }

    @Test
    void testAddNotice_Success() {
        NoticeMapper noticeMapper = Mockito.mock(NoticeMapper.class);
        NoticeAsyncService noticeAsyncService = Mockito.mock(NoticeAsyncService.class);
        NoticeServiceImpl service = new NoticeServiceImpl(noticeMapper, noticeAsyncService);

        UserContext.setCurrentUserId(2001L);
        Mockito.when(noticeMapper.insert(Mockito.any(Notice.class))).thenAnswer(invocation -> {
            Notice saved = invocation.getArgument(0);
            saved.setId(9L);
            return 1;
        });

        NoticeDTO dto = new NoticeDTO();
        dto.setTitle("上架公告");
        dto.setContent("内容");
        dto.setType(1);
        dto.setStatus(0);
        service.addNotice(dto);

        ArgumentCaptor<Notice> captor = ArgumentCaptor.forClass(Notice.class);
        Mockito.verify(noticeMapper).insert(captor.capture());
        Assertions.assertEquals(2001L, captor.getValue().getPublisherId());
        Mockito.verifyNoInteractions(noticeAsyncService);
    }

    @Test
    void testAddNotice_WithPushNotification() {
        NoticeMapper noticeMapper = Mockito.mock(NoticeMapper.class);
        NoticeAsyncService noticeAsyncService = Mockito.mock(NoticeAsyncService.class);
        NoticeServiceImpl service = new NoticeServiceImpl(noticeMapper, noticeAsyncService);

        UserContext.setCurrentUserId(2002L);
        Mockito.when(noticeMapper.insert(Mockito.any(Notice.class))).thenAnswer(invocation -> {
            Notice saved = invocation.getArgument(0);
            saved.setId(10L);
            return 1;
        });

        NoticeDTO dto = new NoticeDTO();
        dto.setTitle("系统公告");
        dto.setContent("系统内容");
        dto.setType(1);
        dto.setStatus(1);
        service.addNotice(dto);

        Mockito.verify(noticeAsyncService).pushNoticeToAllUsers(10L, "系统公告", "系统内容");
    }

    @Test
    void testUpdateNotice_Success() {
        NoticeMapper noticeMapper = Mockito.mock(NoticeMapper.class);
        NoticeAsyncService noticeAsyncService = Mockito.mock(NoticeAsyncService.class);
        NoticeServiceImpl service = new NoticeServiceImpl(noticeMapper, noticeAsyncService);

        Mockito.when(noticeMapper.selectById(11L)).thenReturn(buildNotice(11L, "旧", "旧", 1, 1));
        Mockito.when(noticeMapper.updateById(Mockito.any(Notice.class))).thenReturn(1);

        NoticeDTO dto = new NoticeDTO();
        dto.setId(11L);
        dto.setTitle("新标题");
        dto.setContent("新内容");
        dto.setType(2);
        dto.setStatus(0);
        service.updateNotice(dto);

        Mockito.verify(noticeMapper).updateById(Mockito.any(Notice.class));
    }

    @Test
    void testUpdateNotice_NotFound() {
        NoticeMapper noticeMapper = Mockito.mock(NoticeMapper.class);
        NoticeAsyncService noticeAsyncService = Mockito.mock(NoticeAsyncService.class);
        NoticeServiceImpl service = new NoticeServiceImpl(noticeMapper, noticeAsyncService);

        Mockito.when(noticeMapper.selectById(12L)).thenReturn(null);

        NoticeDTO dto = new NoticeDTO();
        dto.setId(12L);
        dto.setTitle("新标题");
        dto.setContent("新内容");
        dto.setType(1);
        dto.setStatus(1);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.updateNotice(dto));
        Assertions.assertEquals("公告不存在", ex.getMsg());
    }

    @Test
    void testDeleteNotice_Success() {
        NoticeMapper noticeMapper = Mockito.mock(NoticeMapper.class);
        NoticeAsyncService noticeAsyncService = Mockito.mock(NoticeAsyncService.class);
        NoticeServiceImpl service = new NoticeServiceImpl(noticeMapper, noticeAsyncService);

        Mockito.when(noticeMapper.selectById(13L)).thenReturn(buildNotice(13L, "A", "B", 1, 1));
        Mockito.when(noticeMapper.deleteById(13L)).thenReturn(1);

        service.deleteNotice(13L);

        Mockito.verify(noticeMapper).deleteById(13L);
    }

    @Test
    void testDeleteNotice_NotFound() {
        NoticeMapper noticeMapper = Mockito.mock(NoticeMapper.class);
        NoticeAsyncService noticeAsyncService = Mockito.mock(NoticeAsyncService.class);
        NoticeServiceImpl service = new NoticeServiceImpl(noticeMapper, noticeAsyncService);

        Mockito.when(noticeMapper.selectById(14L)).thenReturn(null);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.deleteNotice(14L));
        Assertions.assertEquals("公告不存在", ex.getMsg());
    }

    @Test
    void testPushNoticeToAllUsers_BatchInsert() {
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        NotificationMapper notificationMapper = Mockito.mock(NotificationMapper.class);
        NoticeAsyncService asyncService = new NoticeAsyncService(userMapper, notificationMapper);

        List<User> users = new ArrayList<>();
        for (int i = 0; i < 2500; i++) {
            User user = new User();
            user.setId(10000L + i);
            user.setStatus(UserStatus.NORMAL.getCode());
            users.add(user);
        }
        Mockito.when(userMapper.selectList(Mockito.any())).thenReturn(users);
        Mockito.when(notificationMapper.insertBatch(Mockito.anyList())).thenReturn(1000);

        asyncService.pushNoticeToAllUsers(20L, "公告标题", "公告内容");

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(notificationMapper, Mockito.times(3)).insertBatch(captor.capture());
        List<List<Notification>> batches = captor.getAllValues();
        Assertions.assertEquals(1000, batches.get(0).size());
        Assertions.assertEquals(1000, batches.get(1).size());
        Assertions.assertEquals(500, batches.get(2).size());
        Notification sample = batches.get(0).get(0);
        Assertions.assertEquals(20L, sample.getRelatedId());
        Assertions.assertEquals("公告标题", sample.getTitle());
        Assertions.assertEquals("公告内容", sample.getContent());
        Assertions.assertEquals(0, sample.getIsRead());
    }

    private Notice buildNotice(Long id, String title, String content, Integer type, Integer status) {
        Notice notice = new Notice();
        notice.setId(id);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setType(type);
        notice.setStatus(status);
        notice.setPublisherId(100L);
        return notice;
    }
}
