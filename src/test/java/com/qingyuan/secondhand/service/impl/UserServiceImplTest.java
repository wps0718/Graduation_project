package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.util.PhoneUtil;
import com.qingyuan.secondhand.common.util.JwtUtil;
import com.qingyuan.secondhand.config.WxConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.dto.AccountLoginDTO;
import com.qingyuan.secondhand.dto.SmsLoginDTO;
import com.qingyuan.secondhand.dto.SmsSendDTO;
import com.qingyuan.secondhand.dto.UserUpdateDTO;
import com.qingyuan.secondhand.dto.WxLoginDTO;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.vo.LoginVO;
import com.qingyuan.secondhand.vo.ProductSimpleVO;
import com.qingyuan.secondhand.vo.UserInfoVO;
import com.qingyuan.secondhand.vo.UserProfileVO;
import com.qingyuan.secondhand.vo.UserStatsVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private Integer readAgreementAccepted(LoginVO vo) {
        try {
            var field = LoginVO.class.getDeclaredField("agreementAccepted");
            field.setAccessible(true);
            return (Integer) field.get(vo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeAcceptAgreement(UserServiceImpl service) {
        try {
            var method = UserServiceImpl.class.getMethod("acceptAgreement");
            method.invoke(service);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(e);
        }
    }

    @Test
    void testWxLogin_ExistingUser() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        Mockito.when(wxConfig.getAppId()).thenReturn("appId");
        Mockito.when(wxConfig.getAppSecret()).thenReturn("appSecret");
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(Map.class), Mockito.anyMap()))
                .thenReturn(Map.of("openid", "openid-1", "session_key", "sk-1"));

        User existing = new User();
        existing.setId(1L);
        existing.setOpenId("openid-1");
        existing.setSessionKey("sk-old");
        existing.setNickName("老用户");
        existing.setAvatarUrl("a.png");
        existing.setAuthStatus(2);
        existing.setStatus(1);
        existing.setAgreementAccepted(1);

        Mockito.when(userMapper.selectByOpenId("openid-1")).thenReturn(existing);
        Mockito.when(jwtUtil.createToken(Mockito.eq(1L), Mockito.anyMap())).thenReturn("token-1");

        UserServiceImpl service = Mockito.spy(new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper()));
        Mockito.doReturn(true).when(service).updateById(Mockito.any(User.class));

        WxLoginDTO dto = new WxLoginDTO();
        dto.setCode("code");
        LoginVO vo = service.wxLogin(dto);

        Assertions.assertEquals(1L, vo.getUserId());
        Assertions.assertEquals(false, vo.getIsNew());
        Assertions.assertEquals(2, vo.getAuthStatus());
        Assertions.assertEquals("老用户", vo.getNickName());
        Assertions.assertEquals("a.png", vo.getAvatarUrl());
        Assertions.assertEquals("token-1", vo.getToken());
        Assertions.assertEquals(1, readAgreementAccepted(vo));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(service).updateById(userCaptor.capture());
        Assertions.assertEquals("sk-1", userCaptor.getValue().getSessionKey());
        Assertions.assertNotNull(userCaptor.getValue().getLastLoginTime());
    }

    @Test
    void testWxLogin_NewUser() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        Mockito.when(wxConfig.getAppId()).thenReturn("appId");
        Mockito.when(wxConfig.getAppSecret()).thenReturn("appSecret");
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(Map.class), Mockito.anyMap()))
                .thenReturn(Map.of("openid", "openid-2", "session_key", "sk-2"));

        Mockito.when(userMapper.selectByOpenId("openid-2")).thenReturn(null);
        Mockito.when(jwtUtil.createToken(Mockito.eq(10001L), Mockito.anyMap())).thenReturn("token-2");

        UserServiceImpl service = Mockito.spy(new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper()));
        Mockito.doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10001L);
            return true;
        }).when(service).save(Mockito.any(User.class));

        WxLoginDTO dto = new WxLoginDTO();
        dto.setCode("code");
        LoginVO vo = service.wxLogin(dto);

        Assertions.assertEquals(10001L, vo.getUserId());
        Assertions.assertEquals(true, vo.getIsNew());
        Assertions.assertEquals("微信用户", vo.getNickName());
        Assertions.assertNotNull(vo.getToken());
        Assertions.assertEquals("token-2", vo.getToken());
        Assertions.assertEquals(0, readAgreementAccepted(vo));
    }

    @Test
    void testWxLogin_BannedUser() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        Mockito.when(wxConfig.getAppId()).thenReturn("appId");
        Mockito.when(wxConfig.getAppSecret()).thenReturn("appSecret");
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(Map.class), Mockito.anyMap()))
                .thenReturn(Map.of("openid", "openid-3", "session_key", "sk-3"));

        User banned = new User();
        banned.setId(3L);
        banned.setOpenId("openid-3");
        banned.setStatus(0);
        banned.setBanReason("违规");

        Mockito.when(userMapper.selectByOpenId("openid-3")).thenReturn(banned);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());
        WxLoginDTO dto = new WxLoginDTO();
        dto.setCode("code");

        Assertions.assertThrows(BusinessException.class, () -> service.wxLogin(dto));
    }

    @Test
    void testWxLogin_DeactivatingUser() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        Mockito.when(wxConfig.getAppId()).thenReturn("appId");
        Mockito.when(wxConfig.getAppSecret()).thenReturn("appSecret");
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(Map.class), Mockito.anyMap()))
                .thenReturn(Map.of("openid", "openid-4", "session_key", "sk-4"));

        User deactivating = new User();
        deactivating.setId(4L);
        deactivating.setOpenId("openid-4");
        deactivating.setNickName("注销中用户");
        deactivating.setAvatarUrl("");
        deactivating.setAuthStatus(0);
        deactivating.setStatus(2);
        deactivating.setScore(BigDecimal.valueOf(5.0));
        deactivating.setLastLoginTime(LocalDateTime.now());
        deactivating.setAgreementAccepted(0);

        Mockito.when(userMapper.selectByOpenId("openid-4")).thenReturn(deactivating);
        Mockito.when(jwtUtil.createToken(Mockito.eq(4L), Mockito.anyMap())).thenReturn("token-4");

        UserServiceImpl service = Mockito.spy(new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper()));
        Mockito.doReturn(true).when(service).updateById(Mockito.any(User.class));

        WxLoginDTO dto = new WxLoginDTO();
        dto.setCode("code");
        LoginVO vo = service.wxLogin(dto);

        Assertions.assertEquals(4L, vo.getUserId());
        Assertions.assertEquals(false, vo.getIsNew());
        Assertions.assertEquals(true, vo.getDeactivating());
        Assertions.assertEquals("token-4", vo.getToken());
        Assertions.assertEquals(0, readAgreementAccepted(vo));
    }

    @Test
    void testAccountLogin_Success() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);

        User user = new User();
        user.setId(10L);
        user.setPhone("13800000000");
        user.setPassword("hash");
        user.setNickName("张三");
        user.setAvatarUrl("a.png");
        user.setAuthStatus(0);
        user.setStatus(1);
        user.setAgreementAccepted(1);

        Mockito.when(userMapper.selectByPhone("13800000000")).thenReturn(user);
        Mockito.when(valueOps.get("login:fail:13800000000")).thenReturn(null);
        Mockito.when(passwordEncoder.matches(Mockito.eq("123456"), Mockito.eq("hash"))).thenReturn(true);
        Mockito.when(jwtUtil.createToken(Mockito.eq(10L), Mockito.anyMap())).thenReturn("token-10");

        UserServiceImpl service = Mockito.spy(new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper()));
        Mockito.doReturn(true).when(service).updateById(Mockito.any(User.class));

        AccountLoginDTO dto = new AccountLoginDTO();
        dto.setPhone("13800000000");
        dto.setPassword("123456");

        LoginVO vo = service.accountLogin(dto);
        Assertions.assertEquals(10L, vo.getUserId());
        Assertions.assertEquals(false, vo.getIsNew());
        Assertions.assertEquals("token-10", vo.getToken());
        Assertions.assertEquals(1, readAgreementAccepted(vo));

        Mockito.verify(stringRedisTemplate).delete("login:fail:13800000000");
        Mockito.verify(service).updateById(Mockito.any(User.class));
    }

    @Test
    void testAccountLogin_UserNotFound() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        Mockito.when(userMapper.selectByPhone("13800000001")).thenReturn(null);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());
        AccountLoginDTO dto = new AccountLoginDTO();
        dto.setPhone("13800000001");
        dto.setPassword("123456");

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.accountLogin(dto));
        Assertions.assertEquals("账号不存在", ex.getMsg());
        Mockito.verifyNoInteractions(stringRedisTemplate);
        Mockito.verifyNoInteractions(passwordEncoder);
    }

    @Test
    void testAccountLogin_PasswordWrong() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);

        User user = new User();
        user.setId(11L);
        user.setPhone("13800000002");
        user.setPassword("hash");
        user.setStatus(1);

        Mockito.when(userMapper.selectByPhone("13800000002")).thenReturn(user);
        Mockito.when(valueOps.get("login:fail:13800000002")).thenReturn(null);
        Mockito.when(passwordEncoder.matches(Mockito.eq("bad"), Mockito.eq("hash"))).thenReturn(false);
        Mockito.when(valueOps.increment("login:fail:13800000002")).thenReturn(2L);
        Mockito.when(stringRedisTemplate.getExpire("login:fail:13800000002", TimeUnit.SECONDS)).thenReturn(600L);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());
        AccountLoginDTO dto = new AccountLoginDTO();
        dto.setPhone("13800000002");
        dto.setPassword("bad");

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.accountLogin(dto));
        Assertions.assertTrue(ex.getMsg().contains("还可尝试 3 次"));
        Mockito.verify(valueOps).increment("login:fail:13800000002");
    }

    @Test
    void testAccountLogin_AccountLocked() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);

        User user = new User();
        user.setId(12L);
        user.setPhone("13800000003");
        user.setPassword("hash");
        user.setStatus(1);

        Mockito.when(userMapper.selectByPhone("13800000003")).thenReturn(user);
        Mockito.when(valueOps.get("login:fail:13800000003")).thenReturn("5");
        Mockito.when(stringRedisTemplate.getExpire("login:fail:13800000003", TimeUnit.SECONDS)).thenReturn(100L);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());
        AccountLoginDTO dto = new AccountLoginDTO();
        dto.setPhone("13800000003");
        dto.setPassword("123456");

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.accountLogin(dto));
        Assertions.assertEquals("账号已锁定，请15分钟后重试", ex.getMsg());
        Mockito.verifyNoInteractions(passwordEncoder);
        Mockito.verify(valueOps, Mockito.never()).increment(Mockito.anyString());
    }

    @Test
    void testAccountLogin_PasswordWrong_FifthAttempt() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);

        User user = new User();
        user.setId(13L);
        user.setPhone("13800000004");
        user.setPassword("hash");
        user.setStatus(1);

        Mockito.when(userMapper.selectByPhone("13800000004")).thenReturn(user);
        Mockito.when(valueOps.get("login:fail:13800000004")).thenReturn(null);
        Mockito.when(passwordEncoder.matches(Mockito.eq("bad"), Mockito.eq("hash"))).thenReturn(false);
        Mockito.when(valueOps.increment("login:fail:13800000004")).thenReturn(5L);
        Mockito.when(stringRedisTemplate.getExpire("login:fail:13800000004", TimeUnit.SECONDS)).thenReturn(600L);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());
        AccountLoginDTO dto = new AccountLoginDTO();
        dto.setPhone("13800000004");
        dto.setPassword("bad");

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.accountLogin(dto));
        Assertions.assertEquals("密码错误次数过多，账号已锁定15分钟", ex.getMsg());
    }

    @Test
    void testAcceptAgreement_SetsAcceptedAndClearsCache() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        User user = new User();
        user.setId(400L);
        user.setAgreementAccepted(0);

        Mockito.when(userMapper.selectById(400L)).thenReturn(user);
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        try {
            UserContext.setCurrentUserId(400L);
            invokeAcceptAgreement(service);
        } finally {
            UserContext.removeCurrentUserId();
        }

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userMapper).updateById(userCaptor.capture());
        Assertions.assertEquals(400L, userCaptor.getValue().getId());
        Assertions.assertEquals(1, userCaptor.getValue().getAgreementAccepted());
        Mockito.verify(stringRedisTemplate).delete("user:info:400");
        Mockito.verify(stringRedisTemplate).delete("user:stats:400");
    }

    @Test
    void testAcceptAgreement_IdempotentWhenAlreadyAccepted() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        User user = new User();
        user.setId(401L);
        user.setAgreementAccepted(1);

        Mockito.when(userMapper.selectById(401L)).thenReturn(user);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        try {
            UserContext.setCurrentUserId(401L);
            invokeAcceptAgreement(service);
        } finally {
            UserContext.removeCurrentUserId();
        }

        Mockito.verify(userMapper, Mockito.never()).updateById(Mockito.any(User.class));
        Mockito.verify(stringRedisTemplate, Mockito.never()).delete(Mockito.anyString());
    }

    @Test
    void testAcceptAgreement_NotLoggedIn() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> invokeAcceptAgreement(service));
        Assertions.assertEquals("未登录", ex.getMsg());
        Mockito.verifyNoInteractions(userMapper);
        Mockito.verifyNoInteractions(stringRedisTemplate);
    }

    @Test
    void testAcceptAgreement_UserNotFound() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        Mockito.when(userMapper.selectById(500L)).thenReturn(null);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        try {
            UserContext.setCurrentUserId(500L);
            BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> invokeAcceptAgreement(service));
            Assertions.assertEquals("用户不存在", ex.getMsg());
        } finally {
            UserContext.removeCurrentUserId();
        }

        Mockito.verify(userMapper, Mockito.never()).updateById(Mockito.any(User.class));
        Mockito.verify(stringRedisTemplate, Mockito.never()).delete(Mockito.anyString());
    }

    @Test
    void testAccountLogin_BannedUser() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);

        User user = new User();
        user.setId(14L);
        user.setPhone("13800000005");
        user.setPassword("hash");
        user.setStatus(0);
        user.setBanReason("违规");

        Mockito.when(userMapper.selectByPhone("13800000005")).thenReturn(user);
        Mockito.when(valueOps.get("login:fail:13800000005")).thenReturn(null);
        Mockito.when(passwordEncoder.matches(Mockito.eq("123456"), Mockito.eq("hash"))).thenReturn(true);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());
        AccountLoginDTO dto = new AccountLoginDTO();
        dto.setPhone("13800000005");
        dto.setPassword("123456");

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.accountLogin(dto));
        Assertions.assertTrue(ex.getMsg().contains("账号已被封禁"));
    }

    @Test
    void testAccountLogin_DeactivatingUser() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);

        User user = new User();
        user.setId(15L);
        user.setPhone("13800000006");
        user.setPassword("hash");
        user.setStatus(2);
        user.setAuthStatus(0);
        user.setNickName("注销中");
        user.setAvatarUrl("");

        Mockito.when(userMapper.selectByPhone("13800000006")).thenReturn(user);
        Mockito.when(valueOps.get("login:fail:13800000006")).thenReturn(null);
        Mockito.when(passwordEncoder.matches(Mockito.eq("123456"), Mockito.eq("hash"))).thenReturn(true);
        Mockito.when(jwtUtil.createToken(Mockito.eq(15L), Mockito.anyMap())).thenReturn("token-15");

        UserServiceImpl service = Mockito.spy(new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper()));
        Mockito.doReturn(true).when(service).updateById(Mockito.any(User.class));

        AccountLoginDTO dto = new AccountLoginDTO();
        dto.setPhone("13800000006");
        dto.setPassword("123456");

        LoginVO vo = service.accountLogin(dto);
        Assertions.assertEquals(true, vo.getDeactivating());
        Assertions.assertEquals("token-15", vo.getToken());
    }

    @Test
    void testAccountLogin_PasswordWrong_FirstAttemptSetsTtl() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);

        User user = new User();
        user.setId(16L);
        user.setPhone("13800000007");
        user.setPassword("hash");
        user.setStatus(1);

        Mockito.when(userMapper.selectByPhone("13800000007")).thenReturn(user);
        Mockito.when(valueOps.get("login:fail:13800000007")).thenReturn(null);
        Mockito.when(passwordEncoder.matches(Mockito.eq("bad"), Mockito.eq("hash"))).thenReturn(false);
        Mockito.when(valueOps.increment("login:fail:13800000007")).thenReturn(1L);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());
        AccountLoginDTO dto = new AccountLoginDTO();
        dto.setPhone("13800000007");
        dto.setPassword("bad");

        Assertions.assertThrows(BusinessException.class, () -> service.accountLogin(dto));

        ArgumentCaptor<Long> timeoutCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(stringRedisTemplate).expire(Mockito.eq("login:fail:13800000007"), timeoutCaptor.capture(), Mockito.eq(TimeUnit.MINUTES));
        Assertions.assertEquals(15L, timeoutCaptor.getValue());
    }

    @Test
    void testSendSmsCode_Success() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        Mockito.when(stringRedisTemplate.hasKey("sms:limit:13800000008")).thenReturn(false);
        Mockito.when(valueOps.get("sms:daily:13800000008")).thenReturn("0");
        Mockito.when(valueOps.increment("sms:daily:13800000008")).thenReturn(2L);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());
        SmsSendDTO dto = new SmsSendDTO();
        dto.setPhone("13800000008");

        service.sendSmsCode(dto);

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(valueOps).set(Mockito.eq("sms:code:13800000008"), codeCaptor.capture(), Mockito.eq(5L), Mockito.eq(TimeUnit.MINUTES));
        Assertions.assertTrue(codeCaptor.getValue().matches("\\d{6}"));

        Mockito.verify(valueOps).set(Mockito.eq("sms:limit:13800000008"), Mockito.eq("1"), Mockito.eq(60L), Mockito.eq(TimeUnit.SECONDS));
        Mockito.verify(valueOps).increment("sms:daily:13800000008");
    }

    @Test
    void testGetUserInfo_CacheMiss_ReturnsMaskedPhoneAndCampusName() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        Mockito.when(valueOps.get("user:info:1")).thenReturn(null);

        User user = new User();
        user.setId(1L);
        user.setNickName("张三");
        user.setAvatarUrl("a.png");
        user.setPhone("13800001234");
        user.setGender(1);
        user.setCampusId(2L);
        user.setAuthStatus(0);
        user.setScore(BigDecimal.valueOf(4.5));
        user.setStatus(1);

        Mockito.when(userMapper.selectById(1L)).thenReturn(user);
        Mockito.when(userMapper.selectCampusNameById(2L)).thenReturn("南海北");

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        try {
            UserContext.setCurrentUserId(1L);
            UserInfoVO vo = service.getUserInfo();
            Assertions.assertEquals(1L, vo.getId());
            Assertions.assertEquals("张三", vo.getNickName());
            Assertions.assertEquals("a.png", vo.getAvatarUrl());
            Assertions.assertEquals("138****1234", vo.getPhone());
            Assertions.assertEquals(2L, vo.getCampusId());
            Assertions.assertEquals("南海北", vo.getCampusName());
            Assertions.assertEquals(0, vo.getAuthStatus());
            Assertions.assertEquals(BigDecimal.valueOf(4.5), vo.getScore());
            Assertions.assertEquals(1, vo.getStatus());
        } finally {
            UserContext.removeCurrentUserId();
        }

        Mockito.verify(valueOps).set(Mockito.eq("user:info:1"), Mockito.anyString(), Mockito.eq(10L), Mockito.eq(TimeUnit.MINUTES));
    }

    @Test
    void testGetUserInfo_CacheHit_DoesNotQueryDb() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        Mockito.when(valueOps.get("user:info:2")).thenReturn("{\"id\":2,\"nickName\":\"李四\",\"avatarUrl\":\"x.png\",\"phone\":\"138****8888\",\"gender\":0,\"campusId\":1,\"campusName\":\"新港\",\"authStatus\":2,\"score\":5.0,\"status\":1}");

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        try {
            UserContext.setCurrentUserId(2L);
            UserInfoVO vo = service.getUserInfo();
            Assertions.assertEquals(2L, vo.getId());
            Assertions.assertEquals("138****8888", vo.getPhone());
            Assertions.assertEquals("新港", vo.getCampusName());
        } finally {
            UserContext.removeCurrentUserId();
        }

        Mockito.verifyNoInteractions(userMapper);
    }

    @Test
    void testGetUserInfo_UserNotFound() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        Mockito.when(valueOps.get("user:info:12")).thenReturn(null);
        Mockito.when(userMapper.selectById(12L)).thenReturn(null);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        try {
            UserContext.setCurrentUserId(12L);
            BusinessException ex = Assertions.assertThrows(BusinessException.class, service::getUserInfo);
            Assertions.assertEquals("用户不存在", ex.getMsg());
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

    @Test
    void testGetUserInfo_NoCampus() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        Mockito.when(valueOps.get("user:info:13")).thenReturn(null);

        User user = new User();
        user.setId(13L);
        user.setNickName("王五");
        user.setAvatarUrl("c.png");
        user.setPhone("13812345678");
        user.setGender(2);
        user.setCampusId(null);
        user.setAuthStatus(1);
        user.setScore(BigDecimal.valueOf(5.0));
        user.setStatus(1);

        Mockito.when(userMapper.selectById(13L)).thenReturn(user);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        try {
            UserContext.setCurrentUserId(13L);
            UserInfoVO vo = service.getUserInfo();
            Assertions.assertEquals(13L, vo.getId());
            Assertions.assertNull(vo.getCampusId());
            Assertions.assertNull(vo.getCampusName());
        } finally {
            UserContext.removeCurrentUserId();
        }

        Mockito.verify(userMapper, Mockito.never()).selectCampusNameById(Mockito.anyLong());
    }

    @Test
    void testUpdateUserInfo_UserNotFound() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        Mockito.when(userMapper.selectById(14L)).thenReturn(null);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setNickName("新昵称");
        dto.setAvatarUrl("b.png");
        dto.setGender(1);

        try {
            UserContext.setCurrentUserId(14L);
            BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.updateUserInfo(dto));
            Assertions.assertEquals("用户不存在", ex.getMsg());
        } finally {
            UserContext.removeCurrentUserId();
        }

        Mockito.verify(userMapper, Mockito.never()).updateById(Mockito.any(User.class));
        Mockito.verify(stringRedisTemplate, Mockito.never()).delete(Mockito.anyString());
    }

    @Test
    void testGetUserProfile_Success() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        User seller = new User();
        seller.setId(100L);
        seller.setNickName("卖家");
        seller.setAvatarUrl("a.png");
        seller.setAuthStatus(2);
        seller.setScore(BigDecimal.valueOf(4.6));

        Mockito.when(userMapper.selectById(100L)).thenReturn(seller);
        Mockito.when(userMapper.countOnSaleProducts(100L)).thenReturn(5);
        Mockito.when(userMapper.countSoldOrders(100L)).thenReturn(3);

        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1L);
        row.put("title", "商品1");
        row.put("price", new BigDecimal("12.34"));
        row.put("images", "[\"1.png\",\"2.png\"]");
        row.put("create_time", now);

        Page<Map<String, Object>> productPage = new Page<>(1, 10);
        productPage.setTotal(1);
        productPage.setRecords(List.of(row));

        Mockito.when(userMapper.pageOnSaleProducts(Mockito.any(Page.class), Mockito.eq(100L))).thenReturn(productPage);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());
        UserProfileVO vo = service.getUserProfile(100L, 1, 10);

        Assertions.assertEquals(100L, vo.getId());
        Assertions.assertEquals("卖家", vo.getNickName());
        Assertions.assertEquals("a.png", vo.getAvatarUrl());
        Assertions.assertEquals(2, vo.getAuthStatus());
        Assertions.assertEquals(BigDecimal.valueOf(4.6), vo.getScore());
        Assertions.assertEquals(5, vo.getOnSaleCount());
        Assertions.assertEquals(3, vo.getSoldCount());

        Assertions.assertNotNull(vo.getProducts());
        Assertions.assertEquals(1, vo.getProducts().getTotal());
        Assertions.assertEquals(1, vo.getProducts().getRecords().size());

        ProductSimpleVO p0 = vo.getProducts().getRecords().get(0);
        Assertions.assertEquals(1L, p0.getId());
        Assertions.assertEquals("商品1", p0.getTitle());
        Assertions.assertEquals(new BigDecimal("12.34"), p0.getPrice());
        Assertions.assertEquals(List.of("1.png", "2.png"), p0.getImages());
        Assertions.assertEquals(now, p0.getCreateTime());
    }

    @Test
    void testGetUserProfile_UserNotFound() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        Mockito.when(userMapper.selectById(101L)).thenReturn(null);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.getUserProfile(101L, 1, 10));
        Assertions.assertEquals("用户不存在", ex.getMsg());
    }

    @Test
    void testDeactivateAccount_Success() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        User user = new User();
        user.setId(200L);
        user.setStatus(1);

        Mockito.when(userMapper.selectById(200L)).thenReturn(user);
        Mockito.when(userMapper.countActiveOrders(200L)).thenReturn(0);
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);
        Mockito.when(userMapper.offShelfAllProducts(200L)).thenReturn(2);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        try {
            UserContext.setCurrentUserId(200L);
            service.deactivateAccount();
        } finally {
            UserContext.removeCurrentUserId();
        }

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userMapper).updateById(userCaptor.capture());
        Assertions.assertEquals(200L, userCaptor.getValue().getId());
        Assertions.assertEquals(2, userCaptor.getValue().getStatus());
        Assertions.assertNotNull(userCaptor.getValue().getDeactivateTime());

        Mockito.verify(userMapper).offShelfAllProducts(200L);
        Mockito.verify(stringRedisTemplate).delete("user:info:200");
        Mockito.verify(stringRedisTemplate).delete("user:stats:200");
    }

    @Test
    void testDeactivateAccount_HasActiveOrders() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        User user = new User();
        user.setId(201L);
        user.setStatus(1);

        Mockito.when(userMapper.selectById(201L)).thenReturn(user);
        Mockito.when(userMapper.countActiveOrders(201L)).thenReturn(1);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        try {
            UserContext.setCurrentUserId(201L);
            BusinessException ex = Assertions.assertThrows(BusinessException.class, service::deactivateAccount);
            Assertions.assertEquals("有进行中的订单，无法注销", ex.getMsg());
        } finally {
            UserContext.removeCurrentUserId();
        }

        Mockito.verify(userMapper, Mockito.never()).updateById(Mockito.any(User.class));
        Mockito.verify(userMapper, Mockito.never()).offShelfAllProducts(Mockito.anyLong());
    }

    @Test
    void testDeactivateAccount_AlreadyDeactivating() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        User user = new User();
        user.setId(202L);
        user.setStatus(2);

        Mockito.when(userMapper.selectById(202L)).thenReturn(user);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        try {
            UserContext.setCurrentUserId(202L);
            BusinessException ex = Assertions.assertThrows(BusinessException.class, service::deactivateAccount);
            Assertions.assertEquals("账号已在注销中", ex.getMsg());
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

    @Test
    void testDeactivateAccount_Banned() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        User user = new User();
        user.setId(203L);
        user.setStatus(0);

        Mockito.when(userMapper.selectById(203L)).thenReturn(user);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        try {
            UserContext.setCurrentUserId(203L);
            BusinessException ex = Assertions.assertThrows(BusinessException.class, service::deactivateAccount);
            Assertions.assertEquals("账号已被封禁，无法注销", ex.getMsg());
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

    @Test
    void testRestoreAccount_Success() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        User user = new User();
        user.setId(300L);
        user.setStatus(2);
        user.setDeactivateTime(LocalDateTime.now().minusDays(1));

        Mockito.when(userMapper.selectById(300L)).thenReturn(user);
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        try {
            UserContext.setCurrentUserId(300L);
            service.restoreAccount();
        } finally {
            UserContext.removeCurrentUserId();
        }

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userMapper).updateById(userCaptor.capture());
        Assertions.assertEquals(300L, userCaptor.getValue().getId());
        Assertions.assertEquals(1, userCaptor.getValue().getStatus());
        Assertions.assertNull(userCaptor.getValue().getDeactivateTime());

        Mockito.verify(stringRedisTemplate).delete("user:info:300");
        Mockito.verify(stringRedisTemplate).delete("user:stats:300");
    }

    @Test
    void testRestoreAccount_NotDeactivating() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        User user = new User();
        user.setId(301L);
        user.setStatus(1);

        Mockito.when(userMapper.selectById(301L)).thenReturn(user);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        try {
            UserContext.setCurrentUserId(301L);
            BusinessException ex = Assertions.assertThrows(BusinessException.class, service::restoreAccount);
            Assertions.assertEquals("账号未在注销中", ex.getMsg());
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

    @Test
    void testPhoneUtil_MaskPhone() {
        Assertions.assertEquals("138****5678", PhoneUtil.maskPhone("13812345678"));
        Assertions.assertEquals("", PhoneUtil.maskPhone(null));
        Assertions.assertEquals("", PhoneUtil.maskPhone(""));
        Assertions.assertEquals("123", PhoneUtil.maskPhone("123"));
    }

    @Test
    void testUpdateUserInfo_Success_UpdatesAndClearsCache() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        Mockito.when(userMapper.selectById(3L)).thenReturn(new User());
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setNickName("新昵称");
        dto.setAvatarUrl("b.png");
        dto.setGender(1);
        dto.setCampusId(3L);

        try {
            UserContext.setCurrentUserId(3L);
            service.updateUserInfo(dto);
        } finally {
            UserContext.removeCurrentUserId();
        }

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userMapper).updateById(userCaptor.capture());
        Assertions.assertEquals(3L, userCaptor.getValue().getId());
        Assertions.assertEquals("新昵称", userCaptor.getValue().getNickName());
        Assertions.assertEquals("b.png", userCaptor.getValue().getAvatarUrl());
        Assertions.assertEquals(1, userCaptor.getValue().getGender());
        Assertions.assertEquals(3L, userCaptor.getValue().getCampusId());

        Mockito.verify(stringRedisTemplate).delete("user:info:3");
        Mockito.verify(stringRedisTemplate).delete("user:stats:3");
    }

    @Test
    void testGetUserStats_CacheHit_DoesNotQueryDb() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        Mockito.when(valueOps.get("user:stats:10")).thenReturn("{\"onSaleCount\":2,\"soldCount\":3,\"favoriteCount\":4}");

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        try {
            UserContext.setCurrentUserId(10L);
            UserStatsVO vo = service.getUserStats();
            Assertions.assertEquals(2, vo.getOnSaleCount());
            Assertions.assertEquals(3, vo.getSoldCount());
            Assertions.assertEquals(4, vo.getFavoriteCount());
        } finally {
            UserContext.removeCurrentUserId();
        }

        Mockito.verifyNoInteractions(userMapper);
    }

    @Test
    void testGetUserStats_CacheMiss_QueriesDbAndCaches() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        Mockito.when(valueOps.get("user:stats:11")).thenReturn(null);
        Mockito.when(userMapper.countOnSaleProducts(11L)).thenReturn(2);
        Mockito.when(userMapper.countSoldOrders(11L)).thenReturn(3);
        Mockito.when(userMapper.countFavoriteProducts(11L)).thenReturn(4);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());

        try {
            UserContext.setCurrentUserId(11L);
            UserStatsVO vo = service.getUserStats();
            Assertions.assertEquals(2, vo.getOnSaleCount());
            Assertions.assertEquals(3, vo.getSoldCount());
            Assertions.assertEquals(4, vo.getFavoriteCount());
        } finally {
            UserContext.removeCurrentUserId();
        }

        Mockito.verify(valueOps).set(Mockito.eq("user:stats:11"), Mockito.anyString(), Mockito.eq(10L), Mockito.eq(TimeUnit.MINUTES));
    }

    @Test
    void testSendSmsCode_FrequencyLimit() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);

        Mockito.when(stringRedisTemplate.hasKey("sms:limit:13800000009")).thenReturn(true);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());
        SmsSendDTO dto = new SmsSendDTO();
        dto.setPhone("13800000009");

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.sendSmsCode(dto));
        Assertions.assertEquals("发送过于频繁，请60秒后重试", ex.getMsg());
        Mockito.verify(stringRedisTemplate, Mockito.never()).opsForValue();
    }

    @Test
    void testSendSmsCode_DailyLimit() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        Mockito.when(stringRedisTemplate.hasKey("sms:limit:13800000010")).thenReturn(false);
        Mockito.when(valueOps.get("sms:daily:13800000010")).thenReturn("10");

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());
        SmsSendDTO dto = new SmsSendDTO();
        dto.setPhone("13800000010");

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.sendSmsCode(dto));
        Assertions.assertEquals("今日发送次数已达上限", ex.getMsg());
        Mockito.verify(valueOps, Mockito.never()).set(Mockito.eq("sms:code:13800000010"), Mockito.anyString(), Mockito.anyLong(), Mockito.any(TimeUnit.class));
    }

    @Test
    void testSendSmsCode_FirstTimeSetsDailyTtl() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        Mockito.when(stringRedisTemplate.hasKey("sms:limit:13800000011")).thenReturn(false);
        Mockito.when(valueOps.get("sms:daily:13800000011")).thenReturn("0");
        Mockito.when(valueOps.increment("sms:daily:13800000011")).thenReturn(1L);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());
        SmsSendDTO dto = new SmsSendDTO();
        dto.setPhone("13800000011");

        service.sendSmsCode(dto);

        Mockito.verify(stringRedisTemplate).expire("sms:daily:13800000011", 24, TimeUnit.HOURS);
    }

    @Test
    void testSmsLogin_Success_ExistingUser() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        Mockito.when(valueOps.get("sms:code:13800000012")).thenReturn("123456");

        User user = new User();
        user.setId(20L);
        user.setPhone("13800000012");
        user.setNickName("李四");
        user.setAvatarUrl("a.png");
        user.setAuthStatus(0);
        user.setStatus(1);
        user.setAgreementAccepted(1);

        Mockito.when(userMapper.selectByPhone("13800000012")).thenReturn(user);
        Mockito.when(jwtUtil.createToken(Mockito.eq(20L), Mockito.anyMap())).thenReturn("token-20");

        UserServiceImpl service = Mockito.spy(new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper()));
        Mockito.doReturn(true).when(service).updateById(Mockito.any(User.class));

        SmsLoginDTO dto = new SmsLoginDTO();
        dto.setPhone("13800000012");
        dto.setSmsCode("123456");

        LoginVO vo = service.smsLogin(dto);
        Assertions.assertEquals(20L, vo.getUserId());
        Assertions.assertEquals(false, vo.getIsNew());
        Assertions.assertEquals("token-20", vo.getToken());
        Assertions.assertEquals(1, readAgreementAccepted(vo));
        Mockito.verify(stringRedisTemplate).delete("sms:code:13800000012");
    }

    @Test
    void testSmsLogin_Success_NewUser() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        Mockito.when(valueOps.get("sms:code:13800000013")).thenReturn("123456");
        Mockito.when(userMapper.selectByPhone("13800000013")).thenReturn(null);
        Mockito.when(jwtUtil.createToken(Mockito.eq(10002L), Mockito.anyMap())).thenReturn("token-10002");

        UserServiceImpl service = Mockito.spy(new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper()));
        Mockito.doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10002L);
            return true;
        }).when(service).save(Mockito.any(User.class));
        Mockito.doReturn(true).when(service).updateById(Mockito.any(User.class));

        SmsLoginDTO dto = new SmsLoginDTO();
        dto.setPhone("13800000013");
        dto.setSmsCode("123456");

        LoginVO vo = service.smsLogin(dto);
        Assertions.assertEquals(10002L, vo.getUserId());
        Assertions.assertEquals(true, vo.getIsNew());
        Assertions.assertEquals("用户0013", vo.getNickName());
        Assertions.assertEquals("token-10002", vo.getToken());
        Assertions.assertEquals(0, readAgreementAccepted(vo));
        Mockito.verify(stringRedisTemplate).delete("sms:code:13800000013");
    }

    @Test
    void testSmsLogin_CodeExpired() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        Mockito.when(valueOps.get("sms:code:13800000014")).thenReturn(null);

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());
        SmsLoginDTO dto = new SmsLoginDTO();
        dto.setPhone("13800000014");
        dto.setSmsCode("123456");

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.smsLogin(dto));
        Assertions.assertEquals("验证码已过期", ex.getMsg());
        Mockito.verify(stringRedisTemplate, Mockito.never()).delete(Mockito.anyString());
        Mockito.verifyNoInteractions(userMapper);
    }

    @Test
    void testSmsLogin_CodeWrong() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        Mockito.when(valueOps.get("sms:code:13800000015")).thenReturn("123456");

        UserServiceImpl service = new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper());
        SmsLoginDTO dto = new SmsLoginDTO();
        dto.setPhone("13800000015");
        dto.setSmsCode("654321");

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.smsLogin(dto));
        Assertions.assertEquals("验证码错误", ex.getMsg());
        Mockito.verify(stringRedisTemplate, Mockito.never()).delete(Mockito.anyString());
        Mockito.verifyNoInteractions(userMapper);
    }

    @Test
    void testSmsLogin_BannedUser() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        Mockito.when(valueOps.get("sms:code:13800000016")).thenReturn("123456");

        User user = new User();
        user.setId(30L);
        user.setPhone("13800000016");
        user.setStatus(0);

        Mockito.when(userMapper.selectByPhone("13800000016")).thenReturn(user);

        UserServiceImpl service = Mockito.spy(new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper()));

        SmsLoginDTO dto = new SmsLoginDTO();
        dto.setPhone("13800000016");
        dto.setSmsCode("123456");

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.smsLogin(dto));
        Assertions.assertEquals("账号已被封禁", ex.getMsg());
        Mockito.verify(stringRedisTemplate).delete("sms:code:13800000016");
        Mockito.verify(service, Mockito.never()).updateById(Mockito.any(User.class));
    }

    @Test
    void testSmsLogin_DeactivatingUser() {
        WxConfig wxConfig = Mockito.mock(WxConfig.class);
        RestTemplate restTemplate = Mockito.mock(RestTemplate.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> valueOps = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        Mockito.when(valueOps.get("sms:code:13800000017")).thenReturn("123456");

        User user = new User();
        user.setId(31L);
        user.setPhone("13800000017");
        user.setNickName("注销中用户");
        user.setAvatarUrl("");
        user.setAuthStatus(0);
        user.setStatus(2);
        user.setAgreementAccepted(0);

        Mockito.when(userMapper.selectByPhone("13800000017")).thenReturn(user);
        Mockito.when(jwtUtil.createToken(Mockito.eq(31L), Mockito.anyMap())).thenReturn("token-31");

        UserServiceImpl service = Mockito.spy(new UserServiceImpl(wxConfig, restTemplate, jwtUtil, userMapper, stringRedisTemplate, passwordEncoder, new ObjectMapper()));
        Mockito.doReturn(true).when(service).updateById(Mockito.any(User.class));

        SmsLoginDTO dto = new SmsLoginDTO();
        dto.setPhone("13800000017");
        dto.setSmsCode("123456");

        LoginVO vo = service.smsLogin(dto);
        Assertions.assertEquals(true, vo.getDeactivating());
        Assertions.assertEquals("token-31", vo.getToken());
        Assertions.assertEquals(0, readAgreementAccepted(vo));
        Mockito.verify(stringRedisTemplate).delete("sms:code:13800000017");
    }
}
