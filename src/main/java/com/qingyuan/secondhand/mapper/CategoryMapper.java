package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingyuan.secondhand.entity.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CategoryMapper extends BaseMapper<Category> {

    @Select("select count(1) from product where category_id = #{categoryId} and is_deleted = 0")
    Long countProductByCategoryId(@Param("categoryId") Long categoryId);
}
