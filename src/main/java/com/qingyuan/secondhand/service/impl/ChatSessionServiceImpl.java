package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.common.util.SessionKeyUtil;
import com.qingyuan.secondhand.dto.ChatSessionCreateDTO;
import com.qingyuan.secondhand.entity.ChatSession;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.ChatSessionMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.ChatSessionService;
import com.qingyuan.secondhand.vo.ChatSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionMapper chatSessionMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ChatSessionVO createSession(ChatSessionCreateDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (dto == null || dto.getPeerId() == null) {
            throw new BusinessException("对方用户ID不能为空");
        }
        if (userId.equals(dto.getPeerId())) {
            throw new BusinessException("不能与自己建立会话");
        }
        User peerUser = userMapper.selectById(dto.getPeerId());
        if (peerUser == null) {
            throw new BusinessException("对方用户不存在");
        }

        ChatSession session = findSession(userId, dto.getPeerId(), dto.getProductId());
        boolean isNew = false;
        if (session == null) {
            createDualSessions(userId, dto.getPeerId(), dto.getProductId());
            isNew = true;
        } else if (Integer.valueOf(1).equals(session.getIsDeleted())) {
            restoreSession(session);
        }

        ChatSessionVO vo = buildSessionVO(userId, dto.getPeerId(), dto.getProductId());
        if (vo == null) {
            vo = ChatSessionVO.builder()
                    .peerId(peerUser.getId())
                    .peerName(peerUser.getNickName())
                    .peerAvatar(peerUser.getAvatarUrl())
                    .peerAuthStatus(peerUser.getAuthStatus())
                    .productId(dto.getProductId())
                    .sessionKey(SessionKeyUtil.buildSessionKey(userId, peerUser.getId(), dto.getProductId()))
                    .unread(0)
                    .isTop(false)
                    .build();
        }
        vo.setIsNew(isNew);
        return vo;
    }

    @Override
    public List<ChatSessionVO> getSessionList() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        List<ChatSessionVO> list = chatSessionMapper.selectSessionListByUserId(userId);
        if (list != null) {
            list.forEach(item -> {
                item.setSessionKey(SessionKeyUtil.buildSessionKey(userId, item.getPeerId(), item.getProductId()));
                item.setProductImage(parseCoverImage(item.getProductImage()));
                if (item.getIsTop() == null) {
                    item.setIsTop(false);
                }
            });
        }
        return list;
    }

    @Override
    @Transactional
    public void deleteSession(Long sessionId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (sessionId == null) {
            throw new BusinessException("会话ID不能为空");
        }
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || Integer.valueOf(1).equals(session.getIsDeleted())) {
            throw new BusinessException("会话不存在");
        }
        if (!userId.equals(session.getUserId())) {
            throw new BusinessException("无权限操作");
        }
        ChatSession update = new ChatSession();
        update.setId(sessionId);
        update.setIsDeleted(1);
        update.setUpdateTime(LocalDateTime.now());
        int updated = chatSessionMapper.updateById(update);
        if (updated <= 0) {
            throw new BusinessException("删除会话失败");
        }
        clearUnreadCache(userId);
    }

    @Override
    @Transactional
    public void toggleTop(Long sessionId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (sessionId == null) {
            throw new BusinessException("会话ID不能为空");
        }
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null || Integer.valueOf(1).equals(session.getIsDeleted())) {
            throw new BusinessException("会话不存在");
        }
        if (!userId.equals(session.getUserId())) {
            throw new BusinessException("无权限操作");
        }
        Integer current = session.getIsTop() == null ? 0 : session.getIsTop();
        ChatSession update = new ChatSession();
        update.setId(sessionId);
        update.setIsTop(current == 1 ? 0 : 1);
        update.setUpdateTime(LocalDateTime.now());
        int updated = chatSessionMapper.updateById(update);
        if (updated <= 0) {
            throw new BusinessException("置顶操作失败");
        }
    }

    @Override
    public Integer getUnreadTotal() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        String key = RedisConstant.IM_UNREAD + userId;
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(cached)) {
            try {
                return Integer.parseInt(cached);
            } catch (NumberFormatException ignored) {
            }
        }
        Integer total = calculateUnreadTotal(userId);
        stringRedisTemplate.opsForValue().set(key, String.valueOf(total));
        return total;
    }

    @Override
    public Integer calculateUnreadTotal(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        return calculateUnreadTotalInternal(userId);
    }

    @Override
    @Transactional
    public void ensureSessionExists(Long userA, Long userB, Long productId) {
        if (userA == null || userB == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (userA.equals(userB)) {
            throw new BusinessException("不能与自己建立会话");
        }
        ChatSession aSession = findSession(userA, userB, productId);
        ChatSession bSession = findSession(userB, userA, productId);
        if (aSession == null || bSession == null) {
            createDualSessions(userA, userB, productId);
            return;
        }
        if (Integer.valueOf(1).equals(aSession.getIsDeleted())) {
            restoreSession(aSession);
        }
        if (Integer.valueOf(1).equals(bSession.getIsDeleted())) {
            restoreSession(bSession);
        }
    }

    @Override
    @Transactional
    public void updateSessionLastMsg(Long userId, Long peerId, Long productId, String lastMsg, Integer lastMsgType) {
        if (userId == null || peerId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        updateLastMsg(userId, peerId, productId, lastMsg, lastMsgType, now);
        updateLastMsg(peerId, userId, productId, lastMsg, lastMsgType, now);
    }

    @Override
    @Transactional
    public void incrementUnread(Long userId, Long peerId, Long productId) {
        if (userId == null || peerId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        int updated = chatSessionMapper.incrementUnread(userId, peerId, productId);
        if (updated <= 0) {
            throw new BusinessException("未读数更新失败");
        }
        clearUnreadCache(userId);
    }

    @Override
    @Transactional
    public void resetUnread(Long userId, Long peerId, Long productId) {
        if (userId == null || peerId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        ChatSession update = new ChatSession();
        update.setUnread(0);
        update.setUpdateTime(LocalDateTime.now());
        LambdaQueryWrapper<ChatSession> wrapper = buildSessionWrapper(userId, peerId, productId);
        int updated = chatSessionMapper.update(update, wrapper);
        if (updated <= 0) {
            throw new BusinessException("未读数清空失败");
        }
        clearUnreadCache(userId);
    }

    private void createDualSessions(Long userA, Long userB, Long productId) {
        LocalDateTime now = LocalDateTime.now();
        ChatSession aSession = buildNewSession(userA, userB, productId, now);
        ChatSession bSession = buildNewSession(userB, userA, productId, now);
        int insertedA = chatSessionMapper.insert(aSession);
        int insertedB = chatSessionMapper.insert(bSession);
        if (insertedA <= 0 || insertedB <= 0) {
            throw new BusinessException("创建会话失败");
        }
    }

    private ChatSession buildNewSession(Long userId, Long peerId, Long productId, LocalDateTime now) {
        return ChatSession.builder()
                .userId(userId)
                .peerId(peerId)
                .productId(productId)
                .lastMsg(null)
                .lastMsgType(null)
                .unread(0)
                .lastTime(now)
                .isTop(0)
                .isDeleted(0)
                .createTime(now)
                .updateTime(now)
                .build();
    }

    private void restoreSession(ChatSession session) {
        ChatSession update = new ChatSession();
        update.setId(session.getId());
        update.setIsDeleted(0);
        update.setUpdateTime(LocalDateTime.now());
        int updated = chatSessionMapper.updateById(update);
        if (updated <= 0) {
            throw new BusinessException("恢复会话失败");
        }
    }

    private ChatSessionVO buildSessionVO(Long userId, Long peerId, Long productId) {
        List<ChatSessionVO> list = chatSessionMapper.selectSessionListByUserId(userId);
        if (list == null) {
            return null;
        }
        for (ChatSessionVO item : list) {
            if (peerId.equals(item.getPeerId()) && matchProductId(productId, item.getProductId())) {
                item.setSessionKey(SessionKeyUtil.buildSessionKey(userId, peerId, productId));
                item.setProductImage(parseCoverImage(item.getProductImage()));
                if (item.getIsTop() == null) {
                    item.setIsTop(false);
                }
                return item;
            }
        }
        return null;
    }

    private ChatSession findSession(Long userId, Long peerId, Long productId) {
        LambdaQueryWrapper<ChatSession> wrapper = buildSessionWrapper(userId, peerId, productId);
        return chatSessionMapper.selectOne(wrapper);
    }

    private LambdaQueryWrapper<ChatSession> buildSessionWrapper(Long userId, Long peerId, Long productId) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getPeerId, peerId);
        if (productId == null) {
            wrapper.isNull(ChatSession::getProductId);
        } else {
            wrapper.eq(ChatSession::getProductId, productId);
        }
        return wrapper;
    }

    private void updateLastMsg(Long userId, Long peerId, Long productId, String lastMsg, Integer lastMsgType, LocalDateTime now) {
        ChatSession update = new ChatSession();
        update.setLastMsg(lastMsg);
        update.setLastMsgType(lastMsgType);
        update.setLastTime(now);
        update.setUpdateTime(now);
        LambdaQueryWrapper<ChatSession> wrapper = buildSessionWrapper(userId, peerId, productId);
        int updated = chatSessionMapper.update(update, wrapper);
        if (updated <= 0) {
            throw new BusinessException("更新会话失败");
        }
    }

    private Integer calculateUnreadTotalInternal(Long userId) {
        List<ChatSession> sessions = chatSessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getIsDeleted, 0));
        int total = 0;
        if (sessions != null) {
            for (ChatSession session : sessions) {
                Integer unread = session.getUnread();
                if (unread != null) {
                    total += unread;
                }
            }
        }
        return total;
    }

    private boolean matchProductId(Long productId, Long targetProductId) {
        if (productId == null) {
            return targetProductId == null;
        }
        return productId.equals(targetProductId);
    }

    private String parseCoverImage(String imagesJson) {
        if (!StringUtils.hasText(imagesJson)) {
            return null;
        }
        try {
            List<String> images = objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {
            });
            if (images == null || images.isEmpty()) {
                return null;
            }
            return images.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    private void clearUnreadCache(Long userId) {
        stringRedisTemplate.delete(RedisConstant.IM_UNREAD + userId);
    }
}
