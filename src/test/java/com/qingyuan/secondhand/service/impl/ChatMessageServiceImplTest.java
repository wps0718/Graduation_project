package com.qingyuan.secondhand.service.impl;

import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.enums.NotificationType;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.entity.ChatMessage;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.ChatMessageMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.ChatSessionService;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.vo.ChatMessageVO;
import com.qingyuan.secondhand.websocket.WebSocketSessionManager;
import com.qingyuan.secondhand.websocket.protocol.ChatPayload;
import com.qingyuan.secondhand.websocket.protocol.WebSocketMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceImplTest {

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @Mock
    private ChatSessionService chatSessionService;

    @Mock
    private WebSocketSessionManager sessionManager;

    @Mock
    private UserMapper userMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ChatMessageServiceImpl chatMessageService;

    @BeforeEach
    void setUp() {
        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        UserContext.setCurrentUserId(10001L);
    }

    @AfterEach
    void tearDown() {
        UserContext.removeCurrentUserId();
    }

    @Test
    void testSaveAndPushMessage_正常发送() {
        Long senderId = 10001L;
        ChatPayload payload = new ChatPayload();
        payload.setReceiverId(10002L);
        payload.setProductId(1L);
        payload.setMsgType(1);
        payload.setContent("你好，这个商品还在吗？");

        User sender = new User();
        sender.setId(senderId);
        sender.setNickName("张三");
        sender.setAvatarUrl("avatar.jpg");

        when(userMapper.selectById(senderId)).thenReturn(sender);
        when(sessionManager.sendToUser(anyLong(), any())).thenReturn(true);
        doAnswer(invocation -> {
            ChatMessage msg = invocation.getArgument(0);
            msg.setId(1L);
            return 1;
        }).when(chatMessageMapper).insert(any(ChatMessage.class));

        Long msgId = chatMessageService.saveAndPushMessage(senderId, payload);

        assertNotNull(msgId);
        assertEquals(1L, msgId);
        verify(chatMessageMapper).insert(any(ChatMessage.class));
        verify(chatSessionService).ensureSessionExists(senderId, 10002L, 1L);
        verify(chatSessionService).updateSessionLastMsg(eq(senderId), eq(10002L), eq(1L), any(), eq(1));
        verify(chatSessionService).incrementUnread(10002L, senderId, 1L);
        verify(valueOperations).increment(RedisConstant.IM_UNREAD + 10002L);
        verify(sessionManager).sendToUser(eq(10002L), any(WebSocketMessage.class));
        verify(notificationService, never()).send(anyLong(), any(), any(), any(), any(), any());
    }

    @Test
    void testSaveAndPushMessage_接收方离线() {
        Long senderId = 10001L;
        ChatPayload payload = new ChatPayload();
        payload.setReceiverId(10002L);
        payload.setProductId(1L);
        payload.setMsgType(1);
        String content = "这是一个超过二十个字符的内容预览测试用例文本";
        payload.setContent(content);

        User sender = new User();
        sender.setId(senderId);
        sender.setNickName("张三");
        sender.setAvatarUrl("avatar.jpg");

        when(userMapper.selectById(senderId)).thenReturn(sender);
        when(sessionManager.sendToUser(anyLong(), any())).thenReturn(false);
        doAnswer(invocation -> {
            ChatMessage msg = invocation.getArgument(0);
            msg.setId(1L);
            return 1;
        }).when(chatMessageMapper).insert(any(ChatMessage.class));

        chatMessageService.saveAndPushMessage(senderId, payload);

        @SuppressWarnings("unchecked")
        org.mockito.ArgumentCaptor<Map<String, String>> paramsCaptor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(notificationService).send(
                eq(10002L),
                eq(NotificationType.NEW_MESSAGE),
                paramsCaptor.capture(),
                eq(1L),
                eq(1),
                eq(1)
        );
        Map<String, String> params = paramsCaptor.getValue();
        assertEquals("张三", params.get("nickName"));
        assertEquals(content.substring(0, 20) + "...", params.get("content"));
    }

    @Test
    void testSaveAndPushMessage_receiverId为空() {
        Long senderId = 10001L;
        ChatPayload payload = new ChatPayload();
        payload.setReceiverId(null);
        payload.setMsgType(1);
        payload.setContent("你好");

        BusinessException exception = assertThrows(BusinessException.class, () -> chatMessageService.saveAndPushMessage(senderId, payload));
        assertEquals("接收者不能为空", exception.getMsg());
    }

    @Test
    void testSaveAndPushMessage_content为空() {
        Long senderId = 10001L;
        ChatPayload payload = new ChatPayload();
        payload.setReceiverId(10002L);
        payload.setMsgType(1);
        payload.setContent("");

        BusinessException exception = assertThrows(BusinessException.class, () -> chatMessageService.saveAndPushMessage(senderId, payload));
        assertEquals("消息内容不能为空", exception.getMsg());
    }

    @Test
    void testSaveAndPushMessage_不能给自己发消息() {
        Long senderId = 10001L;
        ChatPayload payload = new ChatPayload();
        payload.setReceiverId(10001L);
        payload.setMsgType(1);
        payload.setContent("你好");

        BusinessException exception = assertThrows(BusinessException.class, () -> chatMessageService.saveAndPushMessage(senderId, payload));
        assertEquals("不能给自己发消息", exception.getMsg());
    }

    @Test
    void testGetMessageHistory_正常分页() {
        String sessionKey = "10001_10002_1";
        Integer page = 1;
        Integer pageSize = 30;

        ChatMessage msg1 = ChatMessage.builder()
                .id(1L)
                .sessionKey(sessionKey)
                .senderId(10001L)
                .receiverId(10002L)
                .msgType(1)
                .content("你好")
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();

        ChatMessage msg2 = ChatMessage.builder()
                .id(2L)
                .sessionKey(sessionKey)
                .senderId(10002L)
                .receiverId(10001L)
                .msgType(1)
                .content("在的")
                .isRead(1)
                .createTime(LocalDateTime.now())
                .build();

        when(chatMessageMapper.selectMessageCount(sessionKey)).thenReturn(2);
        when(chatMessageMapper.selectMessagesBySessionKey(sessionKey, 0, 30))
                .thenReturn(Arrays.asList(msg1, msg2));

        Map<String, Object> result = chatMessageService.getMessageHistory(sessionKey, page, pageSize);

        assertEquals(2, result.get("total"));
        @SuppressWarnings("unchecked")
        List<ChatMessageVO> records = (List<ChatMessageVO>) result.get("records");
        assertEquals(2, records.size());
        assertTrue(records.get(0).getIsSelf());
        assertFalse(records.get(0).getIsRead());
        assertFalse(records.get(1).getIsSelf());
        assertTrue(records.get(1).getIsRead());
    }

    @Test
    void testSaveAndPushMessage_发送商品卡片() {
        Long senderId = 10001L;
        ChatPayload payload = new ChatPayload();
        payload.setReceiverId(10002L);
        payload.setProductId(1L);
        payload.setMsgType(2); // 商品卡片
        payload.setContent("{\"title\":\"测试商品\",\"price\":99.9}");

        User sender = new User();
        sender.setId(senderId);
        sender.setNickName("张三");

        when(userMapper.selectById(senderId)).thenReturn(sender);
        when(sessionManager.sendToUser(anyLong(), any())).thenReturn(true);
        doAnswer(invocation -> {
            ChatMessage msg = invocation.getArgument(0);
            msg.setId(1L);
            return 1;
        }).when(chatMessageMapper).insert(any(ChatMessage.class));

        chatMessageService.saveAndPushMessage(senderId, payload);

        verify(chatSessionService).updateSessionLastMsg(eq(senderId), eq(10002L), eq(1L), eq("[商品卡片]"), eq(2));
    }

    @Test
    void testSendMessage_正常发送() {
        com.qingyuan.secondhand.dto.ChatMessageSendDTO dto = new com.qingyuan.secondhand.dto.ChatMessageSendDTO();
        dto.setSessionKey("10001_10002_1");
        dto.setType(1);
        dto.setContent("你好");

        User sender = new User();
        sender.setId(10001L);
        sender.setNickName("张三");

        when(userMapper.selectById(10001L)).thenReturn(sender);
        when(sessionManager.sendToUser(anyLong(), any())).thenReturn(true);
        doAnswer(invocation -> {
            ChatMessage msg = invocation.getArgument(0);
            msg.setId(1L);
            return 1;
        }).when(chatMessageMapper).insert(any(ChatMessage.class));

        Long msgId = chatMessageService.sendMessage(dto);

        assertNotNull(msgId);
        assertEquals(1L, msgId);
        verify(chatMessageMapper).insert(any(ChatMessage.class));
    }

    @Test
    void testGetMessageHistory_越权访问() {
        String sessionKey = "10002_10003_1";
        Integer page = 1;
        Integer pageSize = 30;

        BusinessException exception = assertThrows(BusinessException.class, () -> chatMessageService.getMessageHistory(sessionKey, page, pageSize));
        assertEquals("无权查看此会话", exception.getMsg());
    }

    @Test
    void testMarkSessionReadByUserId_正常标记() {
        Long userId = 10001L;
        String sessionKey = "10001_10002_1";

        when(chatMessageMapper.markAsRead(userId, sessionKey)).thenReturn(5);
        when(chatSessionService.calculateUnreadTotal(userId)).thenReturn(10);

        chatMessageService.markSessionReadByUserId(userId, sessionKey);

        verify(chatMessageMapper).markAsRead(userId, sessionKey);
        verify(chatSessionService).resetUnread(userId, 10002L, 1L);
        verify(valueOperations).set(RedisConstant.IM_UNREAD + userId, "10");
    }

    @Test
    void testMarkSessionReadByUserId_越权操作() {
        Long userId = 10001L;
        String sessionKey = "10002_10003_1";

        BusinessException exception = assertThrows(BusinessException.class, () -> chatMessageService.markSessionReadByUserId(userId, sessionKey));
        assertEquals("无权操作此会话", exception.getMsg());
    }
}
