package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.ChatSessionCreateDTO;
import com.qingyuan.secondhand.entity.ChatSession;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.ChatSessionMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.vo.ChatSessionVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatSessionServiceImplTest {

    @Mock
    private ChatSessionMapper chatSessionMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ChatSessionServiceImpl chatSessionService;

    @BeforeEach
    void setUp() {
        UserContext.setCurrentUserId(10001L);
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @AfterEach
    void tearDown() {
        UserContext.removeCurrentUserId();
    }

    @Test
    void testCreateSession_新建会话() {
        ChatSessionCreateDTO dto = new ChatSessionCreateDTO();
        dto.setPeerId(10002L);
        dto.setProductId(1001L);

        User peerUser = new User();
        peerUser.setId(10002L);
        peerUser.setNickName("测试用户");
        peerUser.setAvatarUrl("/avatar.jpg");
        peerUser.setAuthStatus(2);

        when(userMapper.selectById(10002L)).thenReturn(peerUser);
        when(chatSessionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(chatSessionMapper.insert(any(ChatSession.class))).thenReturn(1);

        ChatSessionVO result = chatSessionService.createSession(dto);

        assertNotNull(result);
        assertEquals(10002L, result.getPeerId());
        assertEquals("测试用户", result.getPeerName());
        assertTrue(result.getIsNew());
        verify(chatSessionMapper, times(2)).insert(any(ChatSession.class));
    }

    @Test
    void testCreateSession_已有会话不重复创建() {
        ChatSessionCreateDTO dto = new ChatSessionCreateDTO();
        dto.setPeerId(10002L);
        dto.setProductId(1001L);

        User peerUser = new User();
        peerUser.setId(10002L);
        peerUser.setNickName("测试用户");
        peerUser.setAvatarUrl("/avatar.jpg");
        peerUser.setAuthStatus(2);

        ChatSession existing = ChatSession.builder()
                .id(1L)
                .userId(10001L)
                .peerId(10002L)
                .productId(1001L)
                .isDeleted(0)
                .unread(0)
                .lastTime(LocalDateTime.now())
                .build();

        when(userMapper.selectById(10002L)).thenReturn(peerUser);
        when(chatSessionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        ChatSessionVO result = chatSessionService.createSession(dto);

        assertNotNull(result);
        assertFalse(result.getIsNew());
        verify(chatSessionMapper, never()).insert(any(ChatSession.class));
    }

    @Test
    void testCreateSession_自己跟自己抛异常() {
        ChatSessionCreateDTO dto = new ChatSessionCreateDTO();
        dto.setPeerId(10001L);

        assertThrows(BusinessException.class, () -> chatSessionService.createSession(dto));
    }

    @Test
    void testCreateSession_已删除的会话恢复() {
        ChatSessionCreateDTO dto = new ChatSessionCreateDTO();
        dto.setPeerId(10002L);
        dto.setProductId(1001L);

        User peerUser = new User();
        peerUser.setId(10002L);
        peerUser.setNickName("测试用户");
        peerUser.setAvatarUrl("/avatar.jpg");
        peerUser.setAuthStatus(2);

        ChatSession existing = ChatSession.builder()
                .id(1L)
                .userId(10001L)
                .peerId(10002L)
                .productId(1001L)
                .isDeleted(1)
                .unread(0)
                .lastTime(LocalDateTime.now())
                .build();

        when(userMapper.selectById(10002L)).thenReturn(peerUser);
        when(chatSessionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
        doReturn(1).when(chatSessionMapper).updateById(any(ChatSession.class));

        ChatSessionVO result = chatSessionService.createSession(dto);

        assertNotNull(result);
        assertFalse(result.getIsNew());
        verify(chatSessionMapper).updateById(org.mockito.ArgumentMatchers.<ChatSession>argThat(session -> session.getIsDeleted() == 0));
    }

    @Test
    void testDeleteSession_软删除() {
        Long sessionId = 1L;
        ChatSession session = ChatSession.builder()
                .id(sessionId)
                .userId(10001L)
                .peerId(10002L)
                .isDeleted(0)
                .build();

        when(chatSessionMapper.selectById(sessionId)).thenReturn(session);
        doReturn(1).when(chatSessionMapper).updateById(any(ChatSession.class));

        chatSessionService.deleteSession(sessionId);

        verify(chatSessionMapper).updateById(org.mockito.ArgumentMatchers.<ChatSession>argThat(s -> s.getIsDeleted() == 1));
    }

    @Test
    void testDeleteSession_别人的会话抛异常() {
        Long sessionId = 1L;
        ChatSession session = ChatSession.builder()
                .id(sessionId)
                .userId(10003L)
                .peerId(10002L)
                .isDeleted(0)
                .build();

        when(chatSessionMapper.selectById(sessionId)).thenReturn(session);

        assertThrows(BusinessException.class, () -> chatSessionService.deleteSession(sessionId));
    }

    @Test
    void testToggleTop_切换置顶() {
        Long sessionId = 1L;
        ChatSession session = ChatSession.builder()
                .id(sessionId)
                .userId(10001L)
                .peerId(10002L)
                .isTop(0)
                .build();

        when(chatSessionMapper.selectById(sessionId)).thenReturn(session);
        doReturn(1).when(chatSessionMapper).updateById(any(ChatSession.class));

        chatSessionService.toggleTop(sessionId);

        verify(chatSessionMapper).updateById(org.mockito.ArgumentMatchers.<ChatSession>argThat(s -> s.getIsTop() == 1));
    }

    @Test
    void testGetUnreadTotal_Redis缓存命中() {
        when(valueOperations.get("im:unread:10001")).thenReturn("5");

        Integer total = chatSessionService.getUnreadTotal();

        assertEquals(5, total);
        verify(chatSessionMapper, never()).selectList(any());
    }

    @Test
    void testGetUnreadTotal_Redis未命中查数据库() {
        when(valueOperations.get("im:unread:10001")).thenReturn(null);

        ChatSession session1 = ChatSession.builder().unread(3).build();
        ChatSession session2 = ChatSession.builder().unread(2).build();
        List<ChatSession> sessions = Arrays.asList(session1, session2);

        when(chatSessionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(sessions);

        Integer total = chatSessionService.getUnreadTotal();

        assertEquals(5, total);
        verify(valueOperations).set("im:unread:10001", "5");
    }
}
