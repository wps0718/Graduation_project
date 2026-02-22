package com.qingyuan.secondhand.service;

import com.qingyuan.secondhand.dto.ChatSessionCreateDTO;
import com.qingyuan.secondhand.vo.ChatSessionVO;

import java.util.List;

public interface ChatSessionService {
    ChatSessionVO createSession(ChatSessionCreateDTO dto);

    List<ChatSessionVO> getSessionList();

    void deleteSession(Long sessionId);

    void toggleTop(Long sessionId);

    Integer getUnreadTotal();

    Integer calculateUnreadTotal(Long userId);

    void ensureSessionExists(Long userA, Long userB, Long productId);

    void updateSessionLastMsg(Long userId, Long peerId, Long productId, String lastMsg, Integer lastMsgType);

    void incrementUnread(Long userId, Long peerId, Long productId);

    void resetUnread(Long userId, Long peerId, Long productId);
}
