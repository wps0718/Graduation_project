package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.enums.AuthStatus;
import com.qingyuan.secondhand.common.enums.NotificationType;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.AuthSubmitDTO;
import com.qingyuan.secondhand.entity.CampusAuth;
import com.qingyuan.secondhand.entity.College;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.CampusAuthMapper;
import com.qingyuan.secondhand.mapper.CollegeMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.CampusAuthService;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.vo.AuthPageVO;
import com.qingyuan.secondhand.vo.AuthStatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CampusAuthServiceImpl extends ServiceImpl<CampusAuthMapper, CampusAuth> implements CampusAuthService {

    private final CampusAuthMapper campusAuthMapper;
    private final CollegeMapper collegeMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void submitAuth(AuthSubmitDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        CampusAuth existing = campusAuthMapper.selectByUserId(userId);
        if (existing != null && (Integer.valueOf(0).equals(existing.getStatus()) || Integer.valueOf(1).equals(existing.getStatus()))) {
            throw new BusinessException("认证审核中或已通过，无法重复提交");
        }

        CampusAuth studentNoRecord = campusAuthMapper.selectByStudentNo(dto.getStudentNo());
        if (studentNoRecord != null && studentNoRecord.getUserId() != null && !studentNoRecord.getUserId().equals(userId)) {
            throw new BusinessException("该学号已被其他账号认证");
        }

        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            CampusAuth auth = new CampusAuth();
            auth.setUserId(userId);
            auth.setCollegeId(dto.getCollegeId());
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
        } else {
            CampusAuth auth = new CampusAuth();
            auth.setId(existing.getId());
            auth.setUserId(userId);
            auth.setCollegeId(dto.getCollegeId());
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
        }

        User userUpdate = new User();
        userUpdate.setId(userId);
        userUpdate.setAuthStatus(AuthStatus.PENDING.getCode());
        userUpdate.setUpdateTime(now);
        int updatedUser = userMapper.updateById(userUpdate);
        if (updatedUser <= 0) {
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
            throw new BusinessException("未提交认证");
        }

        College college = auth.getCollegeId() == null ? null : collegeMapper.selectById(auth.getCollegeId());

        AuthStatusVO vo = new AuthStatusVO();
        vo.setStatus(auth.getStatus());
        vo.setCollegeName(college == null ? null : college.getName());
        vo.setStudentNo(auth.getStudentNo());
        vo.setClassName(auth.getClassName());
        vo.setCertImage(auth.getCertImage());
        vo.setRejectReason(auth.getRejectReason());
        vo.setReviewTime(auth.getReviewTime());
        return vo;
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
        notificationService.send(
                auth.getUserId(),
                NotificationType.AUTH_REJECT,
                Map.of("reason", rejectReason),
                auth.getId(),
                3,
                2
        );
    }
}
