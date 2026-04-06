package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.enums.AuthStatus;
import com.qingyuan.secondhand.common.enums.NotificationType;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.AuthSubmitDTO;
import com.qingyuan.secondhand.entity.CampusAuth;
import com.qingyuan.secondhand.entity.CampusAuthHistory;
import com.qingyuan.secondhand.entity.College;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.CampusAuthMapper;
import com.qingyuan.secondhand.mapper.CollegeMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.CampusAuthService;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.vo.AuthPageVO;
import com.qingyuan.secondhand.vo.AuthHistoryVO;
import com.qingyuan.secondhand.vo.AuthStatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CampusAuthServiceImpl extends ServiceImpl<CampusAuthMapper, CampusAuth> implements CampusAuthService {

    private final CampusAuthMapper campusAuthMapper;
    private final CollegeMapper collegeMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public void submitAuth(AuthSubmitDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        CampusAuth existing = campusAuthMapper.selectByUserId(userId);

        CampusAuth studentNoRecord = campusAuthMapper.selectByStudentNo(dto.getStudentNo());
        if (studentNoRecord != null && studentNoRecord.getUserId() != null && !studentNoRecord.getUserId().equals(userId)) {
            throw new BusinessException("该学号已被其他账号认证");
        }

        LocalDateTime now = LocalDateTime.now();
        Long authId;
        if (existing == null) {
            CampusAuth auth = new CampusAuth();
            auth.setUserId(userId);
            auth.setCollegeId(dto.getCollegeId());
            auth.setRealName(dto.getRealName());
            auth.setStudentNo(dto.getStudentNo());
            auth.setClassName(dto.getClassName());
            auth.setCertImage(dto.getCertImage());
            auth.setStatus(0);
            auth.setRejectReason(null);
            auth.setReviewTime(null);
            auth.setReviewerId(null);
            auth.setCreateTime(now);
            auth.setUpdateTime(now);

            int inserted = campusAuthMapper.insert(auth);
            if (inserted <= 0) {
                throw new BusinessException("提交失败");
            }
            authId = auth.getId();
        } else {
            CampusAuth auth = new CampusAuth();
            auth.setId(existing.getId());
            auth.setUserId(userId);
            auth.setCollegeId(dto.getCollegeId());
            auth.setRealName(dto.getRealName());
            auth.setStudentNo(dto.getStudentNo());
            auth.setClassName(dto.getClassName());
            auth.setCertImage(dto.getCertImage());
            auth.setStatus(0);
            auth.setRejectReason(null);
            auth.setReviewTime(null);
            auth.setReviewerId(null);
            auth.setUpdateTime(now);

            int updated = campusAuthMapper.updateById(auth);
            if (updated <= 0) {
                throw new BusinessException("提交失败");
            }
            authId = existing.getId();
        }

        User userUpdate = new User();
        userUpdate.setId(userId);
        userUpdate.setAuthStatus(AuthStatus.PENDING.getCode());
        userUpdate.setUpdateTime(now);
        int updatedUser = userMapper.updateById(userUpdate);
        if (updatedUser <= 0) {
            throw new BusinessException("提交失败");
        }
        stringRedisTemplate.delete(RedisConstant.USER_INFO + userId);
        stringRedisTemplate.delete(RedisConstant.USER_STATS + userId);

        CampusAuthHistory history = new CampusAuthHistory();
        history.setUserId(userId);
        history.setAuthId(authId);
        history.setCollegeId(dto.getCollegeId());
        history.setRealName(dto.getRealName());
        history.setStudentNo(dto.getStudentNo());
        history.setClassName(dto.getClassName());
        history.setCertImage(dto.getCertImage());
        history.setStatus(0);
        history.setRejectReason(null);
        history.setReviewTime(null);
        history.setReviewerId(null);
        history.setCreateTime(now);
        history.setUpdateTime(now);
        int insertedHistory = campusAuthMapper.insertHistory(history);
        if (insertedHistory <= 0) {
            throw new BusinessException("提交失败");
        }
    }

    @Override
    public AuthStatusVO getAuthStatus() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        CampusAuth auth = campusAuthMapper.selectByUserId(userId);
        if (auth == null) {
            AuthStatusVO vo = new AuthStatusVO();
            vo.setStatus(AuthStatus.UNAUTHENTICATED.getCode());
            return vo;
        }

        College college = auth.getCollegeId() == null ? null : collegeMapper.selectById(auth.getCollegeId());

        AuthStatusVO vo = new AuthStatusVO();
        Integer mappedStatus = AuthStatus.UNAUTHENTICATED.getCode();
        if (Integer.valueOf(0).equals(auth.getStatus())) {
            mappedStatus = AuthStatus.PENDING.getCode();
        } else if (Integer.valueOf(1).equals(auth.getStatus())) {
            mappedStatus = AuthStatus.AUTHENTICATED.getCode();
        } else if (Integer.valueOf(2).equals(auth.getStatus())) {
            mappedStatus = AuthStatus.REJECTED.getCode();
        }
        vo.setStatus(mappedStatus);
        vo.setRealName(auth.getRealName());
        vo.setCollegeName(college == null ? null : college.getName());
        vo.setStudentNo(auth.getStudentNo());
        vo.setClassName(auth.getClassName());
        vo.setCertImage(auth.getCertImage());
        vo.setRejectReason(auth.getRejectReason());
        vo.setReviewTime(auth.getReviewTime());
        return vo;
    }

    @Override
    public List<AuthHistoryVO> listAuthHistory() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        List<AuthHistoryVO> records = campusAuthMapper.selectHistoryListByUserId(userId);
        if (records == null) {
            return new ArrayList<>();
        }
        for (AuthHistoryVO record : records) {
            record.setStatus(mapAuditStatusToMiniStatus(record.getStatus()));
        }
        return records;
    }

    @Override
    public AuthHistoryVO getAuthHistoryDetail(Long id) {
        if (id == null) {
            throw new BusinessException("认证历史记录ID不能为空");
        }
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        AuthHistoryVO record = campusAuthMapper.selectHistoryDetailByIdAndUserId(id, userId);
        if (record == null) {
            throw new BusinessException("认证历史记录不存在");
        }
        record.setStatus(mapAuditStatusToMiniStatus(record.getStatus()));
        return record;
    }

    @Override
    public List<AuthHistoryVO> listAuthHistoryByAuthId(Long authId) {
        if (authId == null) {
            throw new BusinessException("认证记录ID不能为空");
        }
        List<AuthHistoryVO> records = campusAuthMapper.selectHistoryListByAuthId(authId);
        return records == null ? new ArrayList<>() : records;
    }

    @Override
    public Page<AuthPageVO> pageAuth(Integer page, Integer size, Integer status, Long collegeId) {
        Page<AuthPageVO> pageRequest = new Page<>(page, size);
        return campusAuthMapper.pageAuthWithDetails(pageRequest, status, collegeId, null);
    }

    @Override
    public AuthPageVO getAuthDetail(Long id) {
        Page<AuthPageVO> pageRequest = new Page<>(1, 1);
        Page<AuthPageVO> result = campusAuthMapper.pageAuthWithDetails(pageRequest, null, null, id);
        if (result == null || result.getRecords() == null || result.getRecords().isEmpty()) {
            throw new BusinessException("认证记录不存在");
        }
        return result.getRecords().get(0);
    }

    @Override
    @Transactional
    public void approveAuth(Long id) {
        if (id == null) {
            throw new BusinessException("认证记录ID不能为空");
        }
        CampusAuth auth = campusAuthMapper.selectById(id);
        if (auth == null) {
            throw new BusinessException("认证记录不存在");
        }
        if (!Integer.valueOf(0).equals(auth.getStatus())) {
            throw new BusinessException("该认证已审核");
        }

        LocalDateTime now = LocalDateTime.now();
        Long reviewerId = UserContext.getCurrentUserId();
        CampusAuth updateAuth = new CampusAuth();
        updateAuth.setId(auth.getId());
        updateAuth.setStatus(1);
        updateAuth.setReviewTime(now);
        updateAuth.setReviewerId(reviewerId);
        updateAuth.setUpdateTime(now);
        int updated = campusAuthMapper.updateById(updateAuth);
        if (updated <= 0) {
            throw new BusinessException("审核失败");
        }

        User userUpdate = new User();
        userUpdate.setId(auth.getUserId());
        userUpdate.setAuthStatus(AuthStatus.AUTHENTICATED.getCode());
        userUpdate.setUpdateTime(now);
        int updatedUser = userMapper.updateById(userUpdate);
        if (updatedUser <= 0) {
            throw new BusinessException("审核失败");
        }
        stringRedisTemplate.delete(RedisConstant.USER_INFO + auth.getUserId());
        stringRedisTemplate.delete(RedisConstant.USER_STATS + auth.getUserId());
        int updatedHistory = campusAuthMapper.updateLatestHistoryReviewByAuthId(auth.getId(), 1, null, now, reviewerId, now);
        if (updatedHistory <= 0) {
            CampusAuthHistory history = new CampusAuthHistory();
            history.setUserId(auth.getUserId());
            history.setAuthId(auth.getId());
            history.setCollegeId(auth.getCollegeId());
            history.setRealName(auth.getRealName());
            history.setStudentNo(auth.getStudentNo());
            history.setClassName(auth.getClassName());
            history.setCertImage(auth.getCertImage());
            history.setStatus(1);
            history.setRejectReason(null);
            history.setReviewTime(now);
            history.setReviewerId(reviewerId);
            history.setCreateTime(now);
            history.setUpdateTime(now);
            campusAuthMapper.insertHistory(history);
        }
        notificationService.send(
                auth.getUserId(),
                NotificationType.AUTH_PASS,
                Map.of(),
                auth.getId(),
                3,
                2
        );
    }

    @Override
    @Transactional
    public void rejectAuth(Long id, String rejectReason) {
        if (id == null) {
            throw new BusinessException("认证记录ID不能为空");
        }
        if (!StringUtils.hasText(rejectReason)) {
            throw new BusinessException("驳回原因不能为空");
        }
        CampusAuth auth = campusAuthMapper.selectById(id);
        if (auth == null) {
            throw new BusinessException("认证记录不存在");
        }
        if (!Integer.valueOf(0).equals(auth.getStatus())) {
            throw new BusinessException("该认证已审核");
        }

        LocalDateTime now = LocalDateTime.now();
        Long reviewerId = UserContext.getCurrentUserId();
        CampusAuth updateAuth = new CampusAuth();
        updateAuth.setId(auth.getId());
        updateAuth.setStatus(2);
        updateAuth.setRejectReason(rejectReason);
        updateAuth.setReviewTime(now);
        updateAuth.setReviewerId(reviewerId);
        updateAuth.setUpdateTime(now);
        int updated = campusAuthMapper.updateById(updateAuth);
        if (updated <= 0) {
            throw new BusinessException("审核失败");
        }

        User userUpdate = new User();
        userUpdate.setId(auth.getUserId());
        userUpdate.setAuthStatus(AuthStatus.REJECTED.getCode());
        userUpdate.setUpdateTime(now);
        int updatedUser = userMapper.updateById(userUpdate);
        if (updatedUser <= 0) {
            throw new BusinessException("审核失败");
        }
        stringRedisTemplate.delete(RedisConstant.USER_INFO + auth.getUserId());
        stringRedisTemplate.delete(RedisConstant.USER_STATS + auth.getUserId());
        int updatedHistory = campusAuthMapper.updateLatestHistoryReviewByAuthId(auth.getId(), 2, rejectReason, now, reviewerId, now);
        if (updatedHistory <= 0) {
            CampusAuthHistory history = new CampusAuthHistory();
            history.setUserId(auth.getUserId());
            history.setAuthId(auth.getId());
            history.setCollegeId(auth.getCollegeId());
            history.setRealName(auth.getRealName());
            history.setStudentNo(auth.getStudentNo());
            history.setClassName(auth.getClassName());
            history.setCertImage(auth.getCertImage());
            history.setStatus(2);
            history.setRejectReason(rejectReason);
            history.setReviewTime(now);
            history.setReviewerId(reviewerId);
            history.setCreateTime(now);
            history.setUpdateTime(now);
            campusAuthMapper.insertHistory(history);
        }
        notificationService.send(
                auth.getUserId(),
                NotificationType.AUTH_REJECT,
                Map.of("reason", rejectReason),
                auth.getId(),
                3,
                2
        );
    }

    private Integer mapAuditStatusToMiniStatus(Integer status) {
        if (Integer.valueOf(0).equals(status)) {
            return AuthStatus.PENDING.getCode();
        }
        if (Integer.valueOf(1).equals(status)) {
            return AuthStatus.AUTHENTICATED.getCode();
        }
        if (Integer.valueOf(2).equals(status)) {
            return AuthStatus.REJECTED.getCode();
        }
        return AuthStatus.UNAUTHENTICATED.getCode();
    }
}
