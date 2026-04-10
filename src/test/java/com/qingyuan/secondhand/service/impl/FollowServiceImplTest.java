package com.qingyuan.secondhand.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.entity.UserFollow;
import com.qingyuan.secondhand.mapper.UserFollowMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.vo.FollowStatsVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @AfterEach
    void clearContext() {
        UserContext.removeCurrentUserId();
    }

    @Test
    void testFollow_NotLogin() {
        FollowServiceImpl service = buildService();
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.follow(2L));
        Assertions.assertEquals("未登录", ex.getMsg());
    }

    @Test
    void testFollow_Self() {
        UserContext.setCurrentUserId(1L);
        FollowServiceImpl service = buildService();
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.follow(1L));
        Assertions.assertEquals("不能关注自己", ex.getMsg());
    }

    @Test
    void testFollow_TargetNotFound() {
        UserContext.setCurrentUserId(1L);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        Mockito.when(userMapper.selectById(2L)).thenReturn(null);
        FollowServiceImpl service = buildService(userMapper);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.follow(2L));
        Assertions.assertEquals("用户不存在", ex.getMsg());
    }

    @Test
    void testFollow_AlreadyFollowed_Idempotent() {
        UserContext.setCurrentUserId(1L);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        Mockito.when(userMapper.selectById(2L)).thenReturn(new User());
        UserFollowMapper followMapper = Mockito.mock(UserFollowMapper.class);
        Mockito.when(followMapper.selectCount(Mockito.any())).thenReturn(1L);
        FollowServiceImpl service = buildService(followMapper, userMapper);

        service.follow(2L);
        Mockito.verify(followMapper, Mockito.never()).insert(Mockito.any(UserFollow.class));
    }

    @Test
    void testFollow_Success_InsertsAndInvalidatesCache() {
        UserContext.setCurrentUserId(1L);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        Mockito.when(userMapper.selectById(2L)).thenReturn(new User());
        UserFollowMapper followMapper = Mockito.mock(UserFollowMapper.class);
        Mockito.when(followMapper.selectCount(Mockito.any())).thenReturn(0L);
        Mockito.when(followMapper.insert(Mockito.any(UserFollow.class))).thenReturn(1);

        StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
        FollowServiceImpl service = buildService(followMapper, userMapper, redis);

        service.follow(2L);

        ArgumentCaptor<UserFollow> captor = ArgumentCaptor.forClass(UserFollow.class);
        Mockito.verify(followMapper).insert(captor.capture());
        Assertions.assertEquals(1L, captor.getValue().getFollowerId());
        Assertions.assertEquals(2L, captor.getValue().getFolloweeId());

        Mockito.verify(redis).delete("follow:check:1:2");
        Mockito.verify(redis).delete("follow:stats:1");
        Mockito.verify(redis).delete("follow:stats:2");
    }

    @Test
    void testUnfollow_Success_DeletesAndInvalidatesCache() {
        UserContext.setCurrentUserId(1L);
        UserFollowMapper followMapper = Mockito.mock(UserFollowMapper.class);
        Mockito.when(followMapper.delete(Mockito.any())).thenReturn(1);
        StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
        FollowServiceImpl service = buildService(followMapper, Mockito.mock(UserMapper.class), redis);

        service.unfollow(2L);

        Mockito.verify(followMapper).delete(Mockito.any());
        Mockito.verify(redis).delete("follow:check:1:2");
        Mockito.verify(redis).delete("follow:stats:1");
        Mockito.verify(redis).delete("follow:stats:2");
    }

    @Test
    void testCheckFollow_NotLogin_ReturnsFalse() {
        FollowServiceImpl service = buildService();
        Assertions.assertFalse(service.checkFollow(2L));
    }

    @Test
    void testCheckFollow_CacheHit_ReturnsTrue() {
        UserContext.setCurrentUserId(1L);
        StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> ops = Mockito.mock(ValueOperations.class);
        Mockito.when(redis.opsForValue()).thenReturn(ops);
        Mockito.when(ops.get("follow:check:1:2")).thenReturn("1");

        FollowServiceImpl service = buildService(Mockito.mock(UserFollowMapper.class), Mockito.mock(UserMapper.class), redis);
        Assertions.assertTrue(service.checkFollow(2L));
    }

    @Test
    void testCheckFollow_CacheMiss_SetsCache() {
        UserContext.setCurrentUserId(1L);
        UserFollowMapper followMapper = Mockito.mock(UserFollowMapper.class);
        Mockito.when(followMapper.selectCount(Mockito.any())).thenReturn(0L);

        StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> ops = Mockito.mock(ValueOperations.class);
        Mockito.when(redis.opsForValue()).thenReturn(ops);
        Mockito.when(ops.get("follow:check:1:2")).thenReturn(null);

        FollowServiceImpl service = buildService(followMapper, Mockito.mock(UserMapper.class), redis);
        Assertions.assertFalse(service.checkFollow(2L));
        Mockito.verify(ops).set(Mockito.eq("follow:check:1:2"), Mockito.eq("0"), Mockito.eq(10L), Mockito.eq(TimeUnit.MINUTES));
    }

    @Test
    void testGetFollowStats_CacheMiss_CachesResult() throws Exception {
        UserFollowMapper followMapper = Mockito.mock(UserFollowMapper.class);
        Mockito.when(followMapper.selectCount(Mockito.any())).thenReturn(5L, 2L);

        StringRedisTemplate redis = Mockito.mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> ops = Mockito.mock(ValueOperations.class);
        Mockito.when(redis.opsForValue()).thenReturn(ops);
        Mockito.when(ops.get("follow:stats:9")).thenReturn(null);

        FollowServiceImpl service = buildService(followMapper, Mockito.mock(UserMapper.class), redis);
        FollowStatsVO vo = service.getFollowStats(9L);

        Assertions.assertEquals(5L, vo.getFollowerCount());
        Assertions.assertEquals(2L, vo.getFollowingCount());
        Mockito.verify(ops).set(Mockito.eq("follow:stats:9"), Mockito.anyString(), Mockito.eq(10L), Mockito.eq(TimeUnit.MINUTES));
    }

    private FollowServiceImpl buildService() {
        return buildService(Mockito.mock(UserFollowMapper.class), Mockito.mock(UserMapper.class), Mockito.mock(StringRedisTemplate.class));
    }

    private FollowServiceImpl buildService(UserMapper userMapper) {
        return buildService(Mockito.mock(UserFollowMapper.class), userMapper, Mockito.mock(StringRedisTemplate.class));
    }

    private FollowServiceImpl buildService(UserFollowMapper followMapper, UserMapper userMapper) {
        return buildService(followMapper, userMapper, Mockito.mock(StringRedisTemplate.class));
    }

    private FollowServiceImpl buildService(UserFollowMapper followMapper, UserMapper userMapper, StringRedisTemplate redis) {
        ObjectMapper objectMapper = new ObjectMapper();
        return new FollowServiceImpl(followMapper, userMapper, redis, objectMapper);
    }
}
