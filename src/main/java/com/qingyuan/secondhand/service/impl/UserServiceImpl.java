package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.enums.AuthStatus;
import com.qingyuan.secondhand.common.enums.UserStatus;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.common.util.JwtUtil;
import com.qingyuan.secondhand.common.util.PhoneUtil;
import com.qingyuan.secondhand.config.WxConfig;
import com.qingyuan.secondhand.dto.AccountLoginDTO;
import com.qingyuan.secondhand.dto.SmsLoginDTO;
import com.qingyuan.secondhand.dto.SmsSendDTO;
import com.qingyuan.secondhand.dto.UserUpdateDTO;
import com.qingyuan.secondhand.dto.WxLoginDTO;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.FollowService;
import com.qingyuan.secondhand.service.UserService;
import com.qingyuan.secondhand.vo.AdminUserDetailVO;
import com.qingyuan.secondhand.vo.AdminUserPageVO;
import com.qingyuan.secondhand.vo.LoginVO;
import com.qingyuan.secondhand.vo.FollowStatsVO;
import com.qingyuan.secondhand.vo.SellerProductVO;
import com.qingyuan.secondhand.vo.UserInfoVO;
import com.qingyuan.secondhand.vo.UserProfileVO;
import com.qingyuan.secondhand.vo.UserStatsVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;
import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final WxConfig wxConfig;
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final FollowService followService;

    @Override
    public LoginVO wxLogin(WxLoginDTO dto) {
        Map<String, Object> session = code2Session(dto.getCode());
        String openId = (String) session.get("openid");
        String sessionKey = (String) session.get("session_key");

        if (!StringUtils.hasText(openId) || !StringUtils.hasText(sessionKey)) {
            throw new BusinessException("微信登录失败");
        }

        User user = userMapper.selectByOpenId(openId);
        boolean isNew;

        if (user == null) {
            user = buildNewWxUser(openId, sessionKey);
            isNew = true;
            boolean saved = save(user);
            if (!saved || user.getId() == null) {
                throw new BusinessException("用户创建失败");
            }
        } else {
            isNew = false;
            if (UserStatus.BANNED.getCode().equals(user.getStatus())) {
                throw new BusinessException("账号已被封禁");
            }
            user.setSessionKey(sessionKey);
            user.setLastLoginTime(LocalDateTime.now());
            user.setIpRegion(resolveClientIpRegion());
            user.setUpdateTime(LocalDateTime.now());
            updateById(user);
        }

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setIsNew(isNew);
        vo.setAuthStatus(user.getAuthStatus());
        vo.setNickName(user.getNickName());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setDeactivating(UserStatus.DEREGISTERING.getCode().equals(user.getStatus()));
        Integer agreementAccepted = user.getAgreementAccepted();
        vo.setAgreementAccepted(agreementAccepted == null ? 0 : agreementAccepted);

        String token = jwtUtil.createToken(user.getId(), Map.of("userId", user.getId(), "type", "mini"));
        vo.setToken(token);
        return vo;
    }

    @Override
    public LoginVO accountLogin(AccountLoginDTO dto) {
        String phone = dto.getPhone();
        String key = RedisConstant.LOGIN_FAIL + phone;

        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            throw new BusinessException("账号不存在");
        }

        String failCountStr = stringRedisTemplate.opsForValue().get(key);
        Long failCount = parseLongOrNull(failCountStr);
        if (failCount != null && failCount >= 5) {
            Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttl == null || ttl > 0) {
                throw new BusinessException("账号已锁定，请15分钟后重试");
            }
        }

        boolean matches = passwordEncoder.matches(dto.getPassword(), user.getPassword());
        if (!matches) {
            Long current = stringRedisTemplate.opsForValue().increment(key);
            ensureFailKeyTtl(key, current);

            if (current != null && current >= 5) {
                throw new BusinessException("密码错误次数过多，账号已锁定15分钟");
            }

            long remaining = 5 - (current == null ? 1 : current);
            throw new BusinessException("密码错误，还可尝试 " + remaining + " 次");
        }

        stringRedisTemplate.delete(key);

        if (UserStatus.BANNED.getCode().equals(user.getStatus())) {
            String reason = user.getBanReason();
            if (StringUtils.hasText(reason)) {
                throw new BusinessException("账号已被封禁：" + reason);
            }
            throw new BusinessException("账号已被封禁");
        }

        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginTime(now);
        user.setIpRegion(resolveClientIpRegion());
        user.setUpdateTime(now);
        updateById(user);

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setIsNew(false);
        vo.setAuthStatus(user.getAuthStatus());
        vo.setNickName(user.getNickName());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setDeactivating(UserStatus.DEREGISTERING.getCode().equals(user.getStatus()));
        Integer agreementAccepted = user.getAgreementAccepted();
        vo.setAgreementAccepted(agreementAccepted == null ? 0 : agreementAccepted);

        String token = jwtUtil.createToken(user.getId(), Map.of("userId", user.getId(), "type", "mini"));
        vo.setToken(token);
        return vo;
    }

    @Override
    public void sendSmsCode(SmsSendDTO dto) {
        String phone = dto.getPhone();
        String limitKey = RedisConstant.SMS_LIMIT + phone;
        String dailyKey = RedisConstant.SMS_DAILY + phone;
        String codeKey = RedisConstant.SMS_CODE + phone;

        Boolean limited = stringRedisTemplate.hasKey(limitKey);
        if (Boolean.TRUE.equals(limited)) {
            throw new BusinessException("发送过于频繁，请60秒后重试");
        }

        String dailyStr = stringRedisTemplate.opsForValue().get(dailyKey);
        Long dailyCount = parseLongOrNull(dailyStr);
        if (dailyCount != null && dailyCount >= 10) {
            throw new BusinessException("今日发送次数已达上限");
        }

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));

        stringRedisTemplate.opsForValue().set(codeKey, code, 5, TimeUnit.MINUTES);
        stringRedisTemplate.opsForValue().set(limitKey, "1", 60, TimeUnit.SECONDS);

        Long incremented = stringRedisTemplate.opsForValue().increment(dailyKey);
        if (incremented != null && incremented == 1) {
            stringRedisTemplate.expire(dailyKey, 24, TimeUnit.HOURS);
        }

        log.info("发送验证码到{}：{}", phone, code);
    }

    @Override
    public LoginVO smsLogin(SmsLoginDTO dto) {
        String phone = dto.getPhone();
        String codeKey = RedisConstant.SMS_CODE + phone;

        String cachedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (!StringUtils.hasText(cachedCode)) {
            throw new BusinessException("验证码已过期");
        }

        if (!cachedCode.equals(dto.getSmsCode())) {
            throw new BusinessException("验证码错误");
        }

        stringRedisTemplate.delete(codeKey);

        User user = userMapper.selectByPhone(phone);
        boolean isNew;

        if (user == null) {
            user = buildNewSmsUser(phone);
            isNew = true;
            boolean saved = save(user);
            if (!saved || user.getId() == null) {
                throw new BusinessException("用户创建失败");
            }
        } else {
            isNew = false;
            if (UserStatus.BANNED.getCode().equals(user.getStatus())) {
                throw new BusinessException("账号已被封禁");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginTime(now);
        user.setIpRegion(resolveClientIpRegion());
        user.setUpdateTime(now);
        updateById(user);

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setIsNew(isNew);
        vo.setAuthStatus(user.getAuthStatus());
        vo.setNickName(user.getNickName());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setDeactivating(UserStatus.DEREGISTERING.getCode().equals(user.getStatus()));
        Integer agreementAccepted = user.getAgreementAccepted();
        vo.setAgreementAccepted(agreementAccepted == null ? 0 : agreementAccepted);

        String token = jwtUtil.createToken(user.getId(), Map.of("userId", user.getId(), "type", "mini"));
        vo.setToken(token);
        return vo;
    }

    @Override
    public UserInfoVO getUserInfo() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        String cacheKey = RedisConstant.USER_INFO + userId;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            try {
                UserInfoVO vo = objectMapper.readValue(cached, UserInfoVO.class);
                Integer auditStatus = userMapper.selectLatestCampusAuthAuditStatus(userId);
                Integer mappedAuthStatus = mapCampusAuthAuditStatus(auditStatus);
                if (mappedAuthStatus != null) {
                    vo.setAuthStatus(mappedAuthStatus);
                }
                return vo;
            } catch (Exception e) {
                stringRedisTemplate.delete(cacheKey);
            }
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        String campusName = null;
        if (user.getCampusId() != null) {
            campusName = userMapper.selectCampusNameById(user.getCampusId());
        }

        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setNickName(user.getNickName());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setPhone(PhoneUtil.maskPhone(user.getPhone()));
        vo.setGender(user.getGender());
        vo.setCampusId(user.getCampusId());
        vo.setCampusName(campusName);
        vo.setAuthStatus(user.getAuthStatus());
        vo.setScore(user.getScore());
        vo.setStatus(user.getStatus());
        vo.setBio(user.getBio());
        Integer auditStatus = userMapper.selectLatestCampusAuthAuditStatus(userId);
        Integer mappedAuthStatus = mapCampusAuthAuditStatus(auditStatus);
        if (mappedAuthStatus != null) {
            vo.setAuthStatus(mappedAuthStatus);
        }

        try {
            String json = objectMapper.writeValueAsString(vo);
            stringRedisTemplate.opsForValue().set(cacheKey, json, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            return vo;
        }

        return vo;
    }

    private Integer mapCampusAuthAuditStatus(Integer auditStatus) {
        if (auditStatus == null) {
            return null;
        }
        if (Integer.valueOf(0).equals(auditStatus)) {
            return AuthStatus.PENDING.getCode();
        }
        if (Integer.valueOf(1).equals(auditStatus)) {
            return AuthStatus.AUTHENTICATED.getCode();
        }
        if (Integer.valueOf(2).equals(auditStatus)) {
            return AuthStatus.REJECTED.getCode();
        }
        return AuthStatus.UNAUTHENTICATED.getCode();
    }

    @Override
    public void updateUserInfo(UserUpdateDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        User existing = userMapper.selectById(userId);
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }

        User user = new User();
        user.setId(userId);
        user.setNickName(dto.getNickName());
        user.setAvatarUrl(dto.getAvatarUrl());
        user.setGender(dto.getGender());
        user.setCampusId(dto.getCampusId());
        user.setBio(dto.getBio());
        user.setUpdateTime(LocalDateTime.now());

        int updated = userMapper.updateById(user);
        if (updated <= 0) {
            throw new BusinessException("更新失败");
        }

        stringRedisTemplate.delete(RedisConstant.USER_INFO + userId);
        stringRedisTemplate.delete(RedisConstant.USER_STATS + userId);
    }

    @Override
    public UserStatsVO getUserStats() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        String cacheKey = RedisConstant.USER_STATS + userId;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, UserStatsVO.class);
            } catch (Exception e) {
                stringRedisTemplate.delete(cacheKey);
            }
        }

        Integer onSaleCount = userMapper.countOnSaleProducts(userId);
        Integer soldCount = userMapper.countSoldOrders(userId);
        Integer favoriteCount = userMapper.countFavoriteProducts(userId);

        UserStatsVO vo = new UserStatsVO();
        vo.setOnSaleCount(onSaleCount == null ? 0 : onSaleCount);
        vo.setSoldCount(soldCount == null ? 0 : soldCount);
        vo.setFavoriteCount(favoriteCount == null ? 0 : favoriteCount);

        try {
            String json = objectMapper.writeValueAsString(vo);
            stringRedisTemplate.opsForValue().set(cacheKey, json, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            return vo;
        }

        return vo;
    }

    @Override
    public UserProfileVO getUserProfile(Long userId, Integer page, Integer pageSize) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        Integer onSaleCount = userMapper.countOnSaleProducts(userId);
        Integer soldCount = userMapper.countSoldOrders(userId);

        Page<SellerProductVO> productsPage = userMapper.pageOnSaleSellerProducts(new Page<>(page, pageSize), userId);
        if (productsPage != null && productsPage.getRecords() != null) {
            productsPage.getRecords().forEach(item -> item.setCoverImage(parseCoverImage(item.getCoverImage())));
        } else {
            productsPage = new Page<>(page, pageSize, 0);
        }

        FollowStatsVO stats = followService.getFollowStats(userId);
        Integer lastActiveDays = calculateLastActiveDays(user.getLastLoginTime());
        String lastActiveText = buildLastActiveText(lastActiveDays, user.getLastLoginTime());

        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setNickName(user.getNickName());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setAuthStatus(user.getAuthStatus());
        vo.setScore(user.getScore());
        vo.setBio(user.getBio());
        vo.setIpRegion(user.getIpRegion());
        vo.setLastActiveDays(lastActiveDays);
        vo.setLastActiveText(lastActiveText);
        vo.setFollowerCount(stats == null ? 0L : stats.getFollowerCount());
        vo.setFollowingCount(stats == null ? 0L : stats.getFollowingCount());
        vo.setOnSaleCount(onSaleCount == null ? 0 : onSaleCount);
        vo.setSoldCount(soldCount == null ? 0 : soldCount);
        vo.setStatus(user.getStatus());
        vo.setProducts(productsPage);
        return vo;
    }

    private Integer calculateLastActiveDays(LocalDateTime lastLoginTime) {
        if (lastLoginTime == null) {
            return null;
        }
        LocalDate last = lastLoginTime.toLocalDate();
        LocalDate now = LocalDate.now();
        long days = ChronoUnit.DAYS.between(last, now);
        if (days < 0) {
            return 0;
        }
        if (days > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) days;
    }

    private String buildLastActiveText(Integer days, LocalDateTime lastLoginTime) {
        if (lastLoginTime == null) {
            return "很久以前来过";
        }
        if (days == null) {
            return "很久以前来过";
        }
        if (days <= 0) {
            return "今天来过";
        }
        return days + "天前来过";
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

    private String resolveClientIpRegion() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        String ip = extractClientIp(request);
        if (!StringUtils.hasText(ip)) {
            return null;
        }
        if (ip.startsWith("127.") || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equalsIgnoreCase(ip)) {
            return "本地";
        }
        return "未知";
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs == null ? null : attrs.getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            String first = forwarded.split(",")[0].trim();
            if (StringUtils.hasText(first)) {
                return first;
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    public void acceptAgreement() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (Integer.valueOf(1).equals(user.getAgreementAccepted())) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        User update = new User();
        update.setId(userId);
        update.setAgreementAccepted(1);
        update.setUpdateTime(now);
        int updated = userMapper.updateById(update);
        if (updated <= 0) {
            throw new BusinessException("协议确认失败");
        }
        stringRedisTemplate.delete(RedisConstant.USER_INFO + userId);
        stringRedisTemplate.delete(RedisConstant.USER_STATS + userId);
    }

    @Override
    @Transactional
    public void deactivateAccount() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (UserStatus.DEREGISTERING.getCode().equals(user.getStatus())) {
            throw new BusinessException("账号已在注销中");
        }
        if (UserStatus.BANNED.getCode().equals(user.getStatus())) {
            throw new BusinessException("账号已被封禁，无法注销");
        }

        Integer activeOrders = userMapper.countActiveOrders(userId);
        if (activeOrders != null && activeOrders > 0) {
            throw new BusinessException("有进行中的订单，无法注销");
        }

        LocalDateTime now = LocalDateTime.now();
        User update = new User();
        update.setId(userId);
        update.setStatus(UserStatus.DEREGISTERING.getCode());
        update.setDeactivateTime(now);
        update.setUpdateTime(now);
        int updated = userMapper.updateById(update);
        if (updated <= 0) {
            throw new BusinessException("注销失败");
        }

        userMapper.offShelfAllProducts(userId);

        stringRedisTemplate.delete(RedisConstant.USER_INFO + userId);
        stringRedisTemplate.delete(RedisConstant.USER_STATS + userId);
    }

    @Override
    public void restoreAccount() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!UserStatus.DEREGISTERING.getCode().equals(user.getStatus())) {
            throw new BusinessException("账号未在注销中");
        }

        LocalDateTime now = LocalDateTime.now();
        User update = new User();
        update.setId(userId);
        update.setStatus(UserStatus.NORMAL.getCode());
        update.setDeactivateTime(null);
        update.setUpdateTime(now);
        int updated = userMapper.updateById(update);
        if (updated <= 0) {
            throw new BusinessException("恢复失败");
        }

        stringRedisTemplate.delete(RedisConstant.USER_INFO + userId);
        stringRedisTemplate.delete(RedisConstant.USER_STATS + userId);
    }

    @Override
    public Page<AdminUserPageVO> getAdminUserPage(Integer page, Integer pageSize, String keyword, Integer status, Integer authStatus, Long campusId) {
        int current = page == null || page <= 0 ? 1 : page;
        int size = pageSize == null || pageSize <= 0 ? 10 : pageSize;
        Page<AdminUserPageVO> result = userMapper.getAdminUserPage(new Page<>(current, size), keyword, status, authStatus, campusId);
        if (result == null || result.getRecords() == null) {
            return result;
        }
        result.getRecords().forEach(item -> item.setPhone(PhoneUtil.maskPhone(item.getPhone())));
        return result;
    }

    @Override
    public AdminUserDetailVO getAdminUserDetail(Long id) {
        if (id == null) {
            throw new BusinessException("用户ID不能为空");
        }
        AdminUserDetailVO detail = userMapper.getAdminUserDetail(id);
        if (detail == null) {
            throw new BusinessException("用户不存在");
        }
        detail.setPhone(PhoneUtil.maskPhone(detail.getPhone()));
        return detail;
    }

    @Override
    public void banUser(Long userId, String banReason) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (UserStatus.BANNED.getCode().equals(user.getStatus())) {
            throw new BusinessException("账号已被封禁");
        }
        if (UserStatus.DEREGISTERING.getCode().equals(user.getStatus())) {
            throw new BusinessException("账号注销中，无法封禁");
        }
        String reason = StringUtils.hasText(banReason) ? banReason : "违规封禁";
        User update = new User();
        update.setId(userId);
        update.setStatus(UserStatus.BANNED.getCode());
        update.setBanReason(reason);
        update.setUpdateTime(LocalDateTime.now());
        int updated = userMapper.updateById(update);
        if (updated <= 0) {
            throw new BusinessException("封禁失败");
        }
        stringRedisTemplate.delete(RedisConstant.USER_INFO + userId);
        stringRedisTemplate.delete(RedisConstant.USER_STATS + userId);
    }

    @Override
    public void unbanUser(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!UserStatus.BANNED.getCode().equals(user.getStatus())) {
            throw new BusinessException("账号未被封禁");
        }
        User update = new User();
        update.setId(userId);
        update.setStatus(UserStatus.NORMAL.getCode());
        update.setBanReason(null);
        update.setUpdateTime(LocalDateTime.now());
        int updated = userMapper.updateById(update);
        if (updated <= 0) {
            throw new BusinessException("解封失败");
        }
        stringRedisTemplate.delete(RedisConstant.USER_INFO + userId);
        stringRedisTemplate.delete(RedisConstant.USER_STATS + userId);
    }

    private User buildNewWxUser(String openId, String sessionKey) {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setOpenId(openId);
        user.setSessionKey(sessionKey);
        user.setNickName("微信用户");
        user.setAvatarUrl("");
        user.setIpRegion(resolveClientIpRegion());
        user.setGender(0);
        user.setAuthStatus(AuthStatus.UNAUTHENTICATED.getCode());
        user.setScore(BigDecimal.valueOf(5.0));
        user.setStatus(UserStatus.NORMAL.getCode());
        user.setAgreementAccepted(0);
        user.setLastLoginTime(now);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        return user;
    }

    private User buildNewSmsUser(String phone) {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setPhone(phone);
        user.setUsername(phone);
        user.setNickName("用户" + phone.substring(phone.length() - 4));
        user.setAvatarUrl("");
        user.setIpRegion(resolveClientIpRegion());
        user.setGender(0);
        user.setAuthStatus(AuthStatus.UNAUTHENTICATED.getCode());
        user.setScore(BigDecimal.valueOf(5.0));
        user.setStatus(UserStatus.NORMAL.getCode());
        user.setAgreementAccepted(0);
        user.setLastLoginTime(now);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        return user;
    }

    private Map<String, Object> code2Session(String code) {
        if (!StringUtils.hasText(wxConfig.getAppId()) || !StringUtils.hasText(wxConfig.getAppSecret())) {
            throw new BusinessException("微信配置缺失");
        }

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid={appId}&secret={appSecret}&js_code={code}&grant_type=authorization_code";
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("appId", wxConfig.getAppId());
        uriVariables.put("appSecret", wxConfig.getAppSecret());
        uriVariables.put("code", code);

        Map<?, ?> resp = restTemplate.getForObject(url, Map.class, uriVariables);
        if (resp == null) {
            throw new BusinessException("微信登录失败");
        }

        Object errCode = resp.get("errcode");
        if (errCode instanceof Number && ((Number) errCode).intValue() != 0) {
            Object errMsg = resp.get("errmsg");
            log.warn("wx jscode2session failed, errcode={}, errmsg={}", errCode, errMsg);
            throw new BusinessException("微信登录失败");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("openid", resp.get("openid"));
        result.put("session_key", resp.get("session_key"));
        return result;
    }

    private Long parseLongOrNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void ensureFailKeyTtl(String key, Long current) {
        if (current != null && current == 1) {
            stringRedisTemplate.expire(key, 15, TimeUnit.MINUTES);
            return;
        }

        Long ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl == null || ttl < 0) {
            stringRedisTemplate.expire(key, 15, TimeUnit.MINUTES);
        }
    }
}
