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
import com.qingyuan.secondhand.service.UserService;
import com.qingyuan.secondhand.vo.LoginVO;
import com.qingyuan.secondhand.vo.UserInfoVO;
import com.qingyuan.secondhand.vo.ProductSimpleVO;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;

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

        String token = jwtUtil.createToken(user.getId(), Map.of("userId", user.getId()));
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
        user.setUpdateTime(now);
        updateById(user);

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setIsNew(false);
        vo.setAuthStatus(user.getAuthStatus());
        vo.setNickName(user.getNickName());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setDeactivating(UserStatus.DEREGISTERING.getCode().equals(user.getStatus()));

        String token = jwtUtil.createToken(user.getId(), Map.of("userId", user.getId()));
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
        user.setUpdateTime(now);
        updateById(user);

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setIsNew(isNew);
        vo.setAuthStatus(user.getAuthStatus());
        vo.setNickName(user.getNickName());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setDeactivating(UserStatus.DEREGISTERING.getCode().equals(user.getStatus()));

        String token = jwtUtil.createToken(user.getId(), Map.of("userId", user.getId()));
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
                return objectMapper.readValue(cached, UserInfoVO.class);
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

        try {
            String json = objectMapper.writeValueAsString(vo);
            stringRedisTemplate.opsForValue().set(cacheKey, json, 10, TimeUnit.MINUTES);
        } catch (Exception e) {
            return vo;
        }

        return vo;
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

        Page<Map<String, Object>> productPage = userMapper.pageOnSaleProducts(new Page<>(page, pageSize), userId);
        List<ProductSimpleVO> products = (productPage == null || productPage.getRecords() == null)
                ? List.of()
                : productPage.getRecords().stream().map(this::mapToProductSimpleVO).toList();

        Page<ProductSimpleVO> productsPage = new Page<>(
                productPage == null ? page : productPage.getCurrent(),
                productPage == null ? pageSize : productPage.getSize(),
                productPage == null ? 0 : productPage.getTotal()
        );
        productsPage.setRecords(products);

        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setNickName(user.getNickName());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setAuthStatus(user.getAuthStatus());
        vo.setScore(user.getScore());
        vo.setOnSaleCount(onSaleCount == null ? 0 : onSaleCount);
        vo.setSoldCount(soldCount == null ? 0 : soldCount);
        vo.setProducts(productsPage);
        return vo;
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

    private ProductSimpleVO mapToProductSimpleVO(Map<String, Object> row) {
        ProductSimpleVO vo = new ProductSimpleVO();
        vo.setId(parseLong(row.get("id")));
        vo.setTitle(row.get("title") == null ? null : String.valueOf(row.get("title")));
        vo.setPrice(parseBigDecimal(row.get("price")));

        String imagesJson = row.get("images") == null ? null : String.valueOf(row.get("images"));
        if (!StringUtils.hasText(imagesJson)) {
            vo.setImages(List.of());
        } else {
            try {
                vo.setImages(objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {
                }));
            } catch (Exception e) {
                vo.setImages(List.of());
            }
        }

        Object createTimeObj = row.containsKey("createTime") ? row.get("createTime") : row.get("create_time");
        vo.setCreateTime(parseLocalDateTime(createTimeObj));
        return vo;
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
    }

    private User buildNewWxUser(String openId, String sessionKey) {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setOpenId(openId);
        user.setSessionKey(sessionKey);
        user.setNickName("微信用户");
        user.setAvatarUrl("");
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
