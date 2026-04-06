package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.entity.CampusAuth;
import com.qingyuan.secondhand.entity.CampusAuthHistory;
import com.qingyuan.secondhand.vo.AuthPageVO;
import com.qingyuan.secondhand.vo.AuthHistoryVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

public interface CampusAuthMapper extends BaseMapper<CampusAuth> {

    default CampusAuth selectByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return selectOne(new LambdaQueryWrapper<CampusAuth>()
                .eq(CampusAuth::getUserId, userId)
                .last("order by id desc limit 1"));
    }

    default CampusAuth selectByStudentNo(String studentNo) {
        if (!StringUtils.hasText(studentNo)) {
            return null;
        }
        return selectOne(new LambdaQueryWrapper<CampusAuth>()
                .eq(CampusAuth::getStudentNo, studentNo)
                .last("order by id desc limit 1"));
    }

    Page<AuthPageVO> pageAuthWithDetails(Page<AuthPageVO> page,
                                         @Param("status") Integer status,
                                         @Param("collegeId") Long collegeId,
                                         @Param("id") Long id);

    int insertHistory(CampusAuthHistory history);

    int updateLatestHistoryReviewByAuthId(@Param("authId") Long authId,
                                          @Param("status") Integer status,
                                          @Param("rejectReason") String rejectReason,
                                          @Param("reviewTime") LocalDateTime reviewTime,
                                          @Param("reviewerId") Long reviewerId,
                                          @Param("updateTime") LocalDateTime updateTime);

    List<AuthHistoryVO> selectHistoryListByUserId(@Param("userId") Long userId);

    List<AuthHistoryVO> selectHistoryListByAuthId(@Param("authId") Long authId);

    AuthHistoryVO selectHistoryDetailByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
