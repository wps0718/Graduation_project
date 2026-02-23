package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.dto.AccountLoginDTO;
import com.qingyuan.secondhand.dto.SmsLoginDTO;
import com.qingyuan.secondhand.dto.SmsSendDTO;
import com.qingyuan.secondhand.dto.UserUpdateDTO;
import com.qingyuan.secondhand.dto.WxLoginDTO;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.vo.AdminUserDetailVO;
import com.qingyuan.secondhand.vo.AdminUserPageVO;
import com.qingyuan.secondhand.vo.LoginVO;
import com.qingyuan.secondhand.vo.UserInfoVO;
import com.qingyuan.secondhand.vo.UserProfileVO;
import com.qingyuan.secondhand.vo.UserStatsVO;

public interface UserService extends IService<User> {

    LoginVO wxLogin(WxLoginDTO dto);

    LoginVO accountLogin(AccountLoginDTO dto);

    void sendSmsCode(SmsSendDTO dto);

    LoginVO smsLogin(SmsLoginDTO dto);

    UserInfoVO getUserInfo();

    void updateUserInfo(UserUpdateDTO dto);

    UserStatsVO getUserStats();

    UserProfileVO getUserProfile(Long userId, Integer page, Integer pageSize);

    void acceptAgreement();

    void deactivateAccount();

    void restoreAccount();

    Page<AdminUserPageVO> getAdminUserPage(Integer page, Integer pageSize, String keyword, Integer status, Integer authStatus, Long campusId);

    AdminUserDetailVO getAdminUserDetail(Long id);

    void banUser(Long userId, String banReason);

    void unbanUser(Long userId);
}
