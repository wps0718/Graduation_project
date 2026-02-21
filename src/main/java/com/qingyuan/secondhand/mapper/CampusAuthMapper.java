package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.entity.CampusAuth;
import com.qingyuan.secondhand.vo.AuthPageVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.util.StringUtils;

public interface CampusAuthMapper extends BaseMapper<CampusAuth> {

    default CampusAuth selectByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return selectOne(new LambdaQueryWrapper<CampusAuth>()
                .eq(CampusAuth::getUserId, userId)
                .last("limit 1"));
    }

    default CampusAuth selectByStudentNo(String studentNo) {
        if (!StringUtils.hasText(studentNo)) {
            return null;
        }
        return selectOne(new LambdaQueryWrapper<CampusAuth>()
                .eq(CampusAuth::getStudentNo, studentNo)
                .last("limit 1"));
    }

    Page<AuthPageVO> pageAuthWithDetails(Page<AuthPageVO> page,
                                         @Param("status") Integer status,
                                         @Param("collegeId") Long collegeId,
                                         @Param("id") Long id);
}
