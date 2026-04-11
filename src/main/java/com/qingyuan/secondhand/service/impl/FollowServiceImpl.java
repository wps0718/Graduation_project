package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.common.enums.NotificationCategory;
import com.qingyuan.secondhand.common.enums.NotificationType;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.entity.UserFollow;
import com.qingyuan.secondhand.mapper.UserFollowMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.FollowService;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.vo.FollowStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowServiceImpl implements FollowService {

    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void follow(Long targetUserId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (targetUserId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (userId.equals(targetUserId)) {
            throw new BusinessException("不能关注自己");
        }
        User target = userMapper.selectById(targetUserId);
        if (target == null) {
            throw new BusinessException("用户不存在");
        }

        Long exist = userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId)
                .eq(UserFollow::getFolloweeId, targetUserId));
        if (exist != null && exist > 0) {
            return;
        }

        UserFollow follow = new UserFollow();
        follow.setFollowerId(userId);
        follow.setFolloweeId(targetUserId);
        try {
            userFollowMapper.insert(follow);
        } catch (DuplicateKeyException e) {
            log.debug("重复关注，忽略 followerId={}, followeeId={}", userId, targetUserId);
            return;
        }

        // 准备通知数据
        User currentUser = userMapper.selectById(userId);
        Map<String, String> params = new HashMap<>();
        params.put("nickName", currentUser.getNickName());
        Long followId = follow.getId();

        // 注册事务同步，在事务提交后发送通知
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notificationService.send(targetUserId, NotificationType.NEW_FOLLOWER, params, followId, 4, NotificationCategory.SYSTEM.getCode());
                }
            });
        } else {
            // 如果没有事务（如在某些非事务测试或逻辑中），直接发送
            notificationService.send(targetUserId, NotificationType.NEW_FOLLOWER, params, followId, 4, NotificationCategory.SYSTEM.getCode());
        }

        invalidateCache(userId, targetUserId);
    }

    @Override
    @Transactional
    public void unfollow(Long targetUserId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (targetUserId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (userId.equals(targetUserId)) {
            throw new BusinessException("不能取关自己");
        }

        userFollowMapper.delete(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId)
                .eq(UserFollow::getFolloweeId, targetUserId));

        invalidateCache(userId, targetUserId);
    }

    @Override
    public boolean checkFollow(Long targetUserId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return false;
        }
        if (targetUserId == null) {
            return false;
        }
        if (userId.equals(targetUserId)) {
            return false;
        }

        String key = RedisConstant.FOLLOW_CHECK + userId + ":" + targetUserId;
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(cached)) {
            return "1".equals(cached);
        }

        Long count = userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId)
                .eq(UserFollow::getFolloweeId, targetUserId));
        boolean followed = count != null && count > 0;
        stringRedisTemplate.opsForValue().set(key, followed ? "1" : "0", 10, TimeUnit.MINUTES);
        return followed;
    }

    @Override
    public FollowStatsVO getFollowStats(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        String key = RedisConstant.FOLLOW_STATS + userId;
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, FollowStatsVO.class);
            } catch (Exception e) {
                stringRedisTemplate.delete(key);
            }
        }

        Long followerCount = userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFolloweeId, userId));
        Long followingCount = userFollowMapper.selectCount(new LambdaQueryWrapper<UserFollow>()
                .eq(UserFollow::getFollowerId, userId));

        FollowStatsVO vo = new FollowStatsVO();
        vo.setFollowerCount(followerCount == null ? 0L : followerCount);
        vo.setFollowingCount(followingCount == null ? 0L : followingCount);

        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(vo), 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            return vo;
        }

        return vo;
    }

    private void invalidateCache(Long followerId, Long followeeId) {
        stringRedisTemplate.delete(RedisConstant.FOLLOW_CHECK + followerId + ":" + followeeId);
        stringRedisTemplate.delete(RedisConstant.FOLLOW_STATS + followerId);
        stringRedisTemplate.delete(RedisConstant.FOLLOW_STATS + followeeId);
    }
}

