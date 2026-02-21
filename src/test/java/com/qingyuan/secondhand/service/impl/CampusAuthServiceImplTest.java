package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.AuthSubmitDTO;
import com.qingyuan.secondhand.entity.CampusAuth;
import com.qingyuan.secondhand.entity.College;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.CampusAuthMapper;
import com.qingyuan.secondhand.mapper.CollegeMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.vo.AuthPageVO;
import com.qingyuan.secondhand.vo.AuthStatusVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class CampusAuthServiceImplTest {

    @Test
    void testSubmitAuth_FirstTime_Success() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        Mockito.when(campusAuthMapper.selectByUserId(1L)).thenReturn(null);
        Mockito.when(campusAuthMapper.selectByStudentNo("20260001")).thenReturn(null);
        Mockito.when(campusAuthMapper.insert(Mockito.any(CampusAuth.class))).thenReturn(1);
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);
        AuthSubmitDTO dto = new AuthSubmitDTO();
        dto.setCollegeId(10L);
        dto.setStudentNo("20260001");
        dto.setClassName("软件1班");
        dto.setCertImage("a.png");

        try {
            UserContext.setCurrentUserId(1L);
            service.submitAuth(dto);
        } finally {
            UserContext.removeCurrentUserId();
        }

        ArgumentCaptor<CampusAuth> authCaptor = ArgumentCaptor.forClass(CampusAuth.class);
        Mockito.verify(campusAuthMapper).insert(authCaptor.capture());
        CampusAuth saved = authCaptor.getValue();
        Assertions.assertEquals(1L, saved.getUserId());
        Assertions.assertEquals(10L, saved.getCollegeId());
        Assertions.assertEquals("20260001", saved.getStudentNo());
        Assertions.assertEquals("软件1班", saved.getClassName());
        Assertions.assertEquals("a.png", saved.getCertImage());
        Assertions.assertEquals(0, saved.getStatus());
        Assertions.assertNull(saved.getRejectReason());
        Assertions.assertNull(saved.getReviewTime());
        Assertions.assertNull(saved.getReviewerId());
        Assertions.assertNotNull(saved.getCreateTime());
        Assertions.assertNotNull(saved.getUpdateTime());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userMapper).updateById(userCaptor.capture());
        Assertions.assertEquals(1L, userCaptor.getValue().getId());
        Assertions.assertEquals(1, userCaptor.getValue().getAuthStatus());
        Assertions.assertNotNull(userCaptor.getValue().getUpdateTime());
    }

    @Test
    void testSubmitAuth_Resubmit_WhenPending() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        CampusAuth existing = new CampusAuth();
        existing.setId(1L);
        existing.setUserId(2L);
        existing.setStatus(0);
        Mockito.when(campusAuthMapper.selectByUserId(2L)).thenReturn(existing);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);
        AuthSubmitDTO dto = new AuthSubmitDTO();
        dto.setCollegeId(10L);
        dto.setStudentNo("20260002");
        dto.setClassName("软件2班");
        dto.setCertImage("b.png");

        try {
            UserContext.setCurrentUserId(2L);
            BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.submitAuth(dto));
            Assertions.assertEquals("认证审核中或已通过，无法重复提交", ex.getMsg());
        } finally {
            UserContext.removeCurrentUserId();
        }

        Mockito.verify(campusAuthMapper, Mockito.never()).insert(Mockito.any(CampusAuth.class));
        Mockito.verify(campusAuthMapper, Mockito.never()).updateById(Mockito.any(CampusAuth.class));
        Mockito.verifyNoInteractions(userMapper);
    }

    @Test
    void testSubmitAuth_Resubmit_WhenApproved() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        CampusAuth existing = new CampusAuth();
        existing.setId(2L);
        existing.setUserId(3L);
        existing.setStatus(1);
        Mockito.when(campusAuthMapper.selectByUserId(3L)).thenReturn(existing);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);
        AuthSubmitDTO dto = new AuthSubmitDTO();
        dto.setCollegeId(10L);
        dto.setStudentNo("20260003");
        dto.setClassName("软件3班");
        dto.setCertImage("c.png");

        try {
            UserContext.setCurrentUserId(3L);
            BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.submitAuth(dto));
            Assertions.assertEquals("认证审核中或已通过，无法重复提交", ex.getMsg());
        } finally {
            UserContext.removeCurrentUserId();
        }

        Mockito.verify(campusAuthMapper, Mockito.never()).insert(Mockito.any(CampusAuth.class));
        Mockito.verify(campusAuthMapper, Mockito.never()).updateById(Mockito.any(CampusAuth.class));
        Mockito.verifyNoInteractions(userMapper);
    }

    @Test
    void testSubmitAuth_Resubmit_WhenRejected_Success() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        CampusAuth existing = new CampusAuth();
        existing.setId(3L);
        existing.setUserId(4L);
        existing.setStatus(2);
        existing.setRejectReason("不清晰");
        Mockito.when(campusAuthMapper.selectByUserId(4L)).thenReturn(existing);

        CampusAuth sameStudentNo = new CampusAuth();
        sameStudentNo.setId(4L);
        sameStudentNo.setUserId(4L);
        sameStudentNo.setStudentNo("20260004");
        Mockito.when(campusAuthMapper.selectByStudentNo("20260004")).thenReturn(sameStudentNo);

        Mockito.when(campusAuthMapper.updateById(Mockito.any(CampusAuth.class))).thenReturn(1);
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);
        AuthSubmitDTO dto = new AuthSubmitDTO();
        dto.setCollegeId(11L);
        dto.setStudentNo("20260004");
        dto.setClassName("软件4班");
        dto.setCertImage("d.png");

        try {
            UserContext.setCurrentUserId(4L);
            service.submitAuth(dto);
        } finally {
            UserContext.removeCurrentUserId();
        }

        ArgumentCaptor<CampusAuth> authCaptor = ArgumentCaptor.forClass(CampusAuth.class);
        Mockito.verify(campusAuthMapper).updateById(authCaptor.capture());
        CampusAuth updated = authCaptor.getValue();
        Assertions.assertEquals(3L, updated.getId());
        Assertions.assertEquals(4L, updated.getUserId());
        Assertions.assertEquals(11L, updated.getCollegeId());
        Assertions.assertEquals("20260004", updated.getStudentNo());
        Assertions.assertEquals("软件4班", updated.getClassName());
        Assertions.assertEquals("d.png", updated.getCertImage());
        Assertions.assertEquals(0, updated.getStatus());
        Assertions.assertNull(updated.getRejectReason());
        Assertions.assertNull(updated.getReviewTime());
        Assertions.assertNull(updated.getReviewerId());
        Assertions.assertNotNull(updated.getUpdateTime());
    }

    @Test
    void testSubmitAuth_StudentNoExists() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        Mockito.when(campusAuthMapper.selectByUserId(5L)).thenReturn(null);

        CampusAuth other = new CampusAuth();
        other.setId(5L);
        other.setUserId(999L);
        other.setStudentNo("20260005");
        Mockito.when(campusAuthMapper.selectByStudentNo("20260005")).thenReturn(other);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);
        AuthSubmitDTO dto = new AuthSubmitDTO();
        dto.setCollegeId(12L);
        dto.setStudentNo("20260005");
        dto.setClassName("软件5班");
        dto.setCertImage("e.png");

        try {
            UserContext.setCurrentUserId(5L);
            BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.submitAuth(dto));
            Assertions.assertEquals("该学号已被其他账号认证", ex.getMsg());
        } finally {
            UserContext.removeCurrentUserId();
        }

        Mockito.verify(campusAuthMapper, Mockito.never()).insert(Mockito.any(CampusAuth.class));
        Mockito.verify(campusAuthMapper, Mockito.never()).updateById(Mockito.any(CampusAuth.class));
        Mockito.verifyNoInteractions(userMapper);
    }

    @Test
    void testGetAuthStatus_Success() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        CampusAuth auth = new CampusAuth();
        auth.setId(10L);
        auth.setUserId(6L);
        auth.setCollegeId(100L);
        auth.setStudentNo("20260006");
        auth.setClassName("软件6班");
        auth.setCertImage("f.png");
        auth.setStatus(2);
        auth.setRejectReason("材料不清晰");
        auth.setReviewTime(LocalDateTime.now());

        Mockito.when(campusAuthMapper.selectByUserId(6L)).thenReturn(auth);

        College college = new College();
        college.setId(100L);
        college.setName("信息学院");
        Mockito.when(collegeMapper.selectById(100L)).thenReturn(college);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);

        AuthStatusVO vo;
        try {
            UserContext.setCurrentUserId(6L);
            vo = service.getAuthStatus();
        } finally {
            UserContext.removeCurrentUserId();
        }

        Assertions.assertEquals(2, vo.getStatus());
        Assertions.assertEquals("信息学院", vo.getCollegeName());
        Assertions.assertEquals("20260006", vo.getStudentNo());
        Assertions.assertEquals("软件6班", vo.getClassName());
        Assertions.assertEquals("f.png", vo.getCertImage());
        Assertions.assertEquals("材料不清晰", vo.getRejectReason());
        Assertions.assertEquals(auth.getReviewTime(), vo.getReviewTime());
    }

    @Test
    void testGetAuthStatus_NotFound() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        Mockito.when(campusAuthMapper.selectByUserId(7L)).thenReturn(null);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);

        try {
            UserContext.setCurrentUserId(7L);
            BusinessException ex = Assertions.assertThrows(BusinessException.class, service::getAuthStatus);
            Assertions.assertEquals("未提交认证", ex.getMsg());
        } finally {
            UserContext.removeCurrentUserId();
        }
    }

    @Test
    void testPageAuth_NoFilter() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        AuthPageVO vo = new AuthPageVO();
        vo.setId(1L);
        vo.setUserId(10L);
        vo.setNickName("测试用户");
        vo.setCollegeName("信息学院");

        Page<AuthPageVO> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.singletonList(vo));
        pageResult.setTotal(1);

        Mockito.when(campusAuthMapper.pageAuthWithDetails(Mockito.any(Page.class), Mockito.isNull(), Mockito.isNull(), Mockito.isNull()))
                .thenReturn(pageResult);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);
        Page<AuthPageVO> result = service.pageAuth(1, 10, null, null);

        Assertions.assertEquals(1, result.getTotal());
        Assertions.assertEquals(1, result.getRecords().size());
        Assertions.assertEquals(1L, result.getRecords().get(0).getId());
        Mockito.verify(campusAuthMapper).pageAuthWithDetails(Mockito.any(Page.class), Mockito.isNull(), Mockito.isNull(), Mockito.isNull());
    }

    @Test
    void testPageAuth_WithStatusFilter() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        Page<AuthPageVO> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.emptyList());

        Mockito.when(campusAuthMapper.pageAuthWithDetails(Mockito.any(Page.class), Mockito.eq(0), Mockito.isNull(), Mockito.isNull()))
                .thenReturn(pageResult);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);
        Page<AuthPageVO> result = service.pageAuth(1, 10, 0, null);

        Assertions.assertEquals(0, result.getRecords().size());
        Mockito.verify(campusAuthMapper).pageAuthWithDetails(Mockito.any(Page.class), Mockito.eq(0), Mockito.isNull(), Mockito.isNull());
    }

    @Test
    void testPageAuth_WithCollegeIdFilter() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        Page<AuthPageVO> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.emptyList());

        Mockito.when(campusAuthMapper.pageAuthWithDetails(Mockito.any(Page.class), Mockito.isNull(), Mockito.eq(2L), Mockito.isNull()))
                .thenReturn(pageResult);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);
        Page<AuthPageVO> result = service.pageAuth(1, 10, null, 2L);

        Assertions.assertEquals(0, result.getRecords().size());
        Mockito.verify(campusAuthMapper).pageAuthWithDetails(Mockito.any(Page.class), Mockito.isNull(), Mockito.eq(2L), Mockito.isNull());
    }

    @Test
    void testGetAuthDetail_Success() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        AuthPageVO vo = new AuthPageVO();
        vo.setId(10L);
        vo.setUserId(20L);
        vo.setNickName("测试用户");

        Page<AuthPageVO> pageResult = new Page<>(1, 1);
        pageResult.setRecords(Collections.singletonList(vo));

        Mockito.when(campusAuthMapper.pageAuthWithDetails(Mockito.any(Page.class), Mockito.isNull(), Mockito.isNull(), Mockito.eq(10L)))
                .thenReturn(pageResult);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);
        AuthPageVO result = service.getAuthDetail(10L);

        Assertions.assertEquals(10L, result.getId());
        Assertions.assertEquals(20L, result.getUserId());
        Assertions.assertEquals("测试用户", result.getNickName());
    }

    @Test
    void testGetAuthDetail_NotFound() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        Page<AuthPageVO> pageResult = new Page<>(1, 1);
        pageResult.setRecords(Collections.emptyList());

        Mockito.when(campusAuthMapper.pageAuthWithDetails(Mockito.any(Page.class), Mockito.isNull(), Mockito.isNull(), Mockito.eq(11L)))
                .thenReturn(pageResult);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.getAuthDetail(11L));
        Assertions.assertEquals("认证记录不存在", ex.getMsg());
    }

    @Test
    void testApproveAuth_Success() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        CampusAuth auth = new CampusAuth();
        auth.setId(1L);
        auth.setUserId(10L);
        auth.setStatus(0);
        Mockito.when(campusAuthMapper.selectById(1L)).thenReturn(auth);
        Mockito.when(campusAuthMapper.updateById(Mockito.any(CampusAuth.class))).thenReturn(1);
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);
        try {
            UserContext.setCurrentUserId(99L);
            service.approveAuth(1L);
        } finally {
            UserContext.removeCurrentUserId();
        }

        ArgumentCaptor<CampusAuth> authCaptor = ArgumentCaptor.forClass(CampusAuth.class);
        Mockito.verify(campusAuthMapper).updateById(authCaptor.capture());
        CampusAuth updatedAuth = authCaptor.getValue();
        Assertions.assertEquals(1, updatedAuth.getStatus());
        Assertions.assertNotNull(updatedAuth.getReviewTime());
        Assertions.assertEquals(99L, updatedAuth.getReviewerId());
        Assertions.assertNotNull(updatedAuth.getUpdateTime());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userMapper).updateById(userCaptor.capture());
        User updatedUser = userCaptor.getValue();
        Assertions.assertEquals(10L, updatedUser.getId());
        Assertions.assertEquals(2, updatedUser.getAuthStatus());
        Assertions.assertNotNull(updatedUser.getUpdateTime());
    }

    @Test
    void testApproveAuth_AlreadyReviewed() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        CampusAuth auth = new CampusAuth();
        auth.setId(2L);
        auth.setUserId(11L);
        auth.setStatus(1);
        Mockito.when(campusAuthMapper.selectById(2L)).thenReturn(auth);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.approveAuth(2L));
        Assertions.assertEquals("该认证已审核", ex.getMsg());
    }

    @Test
    void testRejectAuth_Success() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        CampusAuth auth = new CampusAuth();
        auth.setId(3L);
        auth.setUserId(12L);
        auth.setStatus(0);
        Mockito.when(campusAuthMapper.selectById(3L)).thenReturn(auth);
        Mockito.when(campusAuthMapper.updateById(Mockito.any(CampusAuth.class))).thenReturn(1);
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);
        try {
            UserContext.setCurrentUserId(100L);
            service.rejectAuth(3L, "材料不清晰");
        } finally {
            UserContext.removeCurrentUserId();
        }

        ArgumentCaptor<CampusAuth> authCaptor = ArgumentCaptor.forClass(CampusAuth.class);
        Mockito.verify(campusAuthMapper).updateById(authCaptor.capture());
        CampusAuth updatedAuth = authCaptor.getValue();
        Assertions.assertEquals(2, updatedAuth.getStatus());
        Assertions.assertEquals("材料不清晰", updatedAuth.getRejectReason());
        Assertions.assertNotNull(updatedAuth.getReviewTime());
        Assertions.assertEquals(100L, updatedAuth.getReviewerId());
        Assertions.assertNotNull(updatedAuth.getUpdateTime());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userMapper).updateById(userCaptor.capture());
        User updatedUser = userCaptor.getValue();
        Assertions.assertEquals(12L, updatedUser.getId());
        Assertions.assertEquals(3, updatedUser.getAuthStatus());
        Assertions.assertNotNull(updatedUser.getUpdateTime());
    }

    @Test
    void testRejectAuth_AlreadyReviewed() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        CampusAuth auth = new CampusAuth();
        auth.setId(4L);
        auth.setUserId(13L);
        auth.setStatus(2);
        Mockito.when(campusAuthMapper.selectById(4L)).thenReturn(auth);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.rejectAuth(4L, "材料不清晰"));
        Assertions.assertEquals("该认证已审核", ex.getMsg());
    }

    @Test
    void testRejectAuth_EmptyReason() {
        CampusAuthMapper campusAuthMapper = Mockito.mock(CampusAuthMapper.class);
        CollegeMapper collegeMapper = Mockito.mock(CollegeMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);

        CampusAuthServiceImpl service = new CampusAuthServiceImpl(campusAuthMapper, collegeMapper, userMapper);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.rejectAuth(5L, " "));
        Assertions.assertEquals("驳回原因不能为空", ex.getMsg());
    }
}
