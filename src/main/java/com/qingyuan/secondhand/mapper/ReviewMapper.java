package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingyuan.secondhand.entity.Review;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT AVG((score_desc + score_attitude + score_experience) / 3.0) FROM review WHERE target_id = #{targetId}")
    BigDecimal calculateAverageScore(@Param("targetId") Long targetId);
}
