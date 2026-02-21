package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.entity.Favorite;
import com.qingyuan.secondhand.vo.FavoriteListVO;
import org.apache.ibatis.annotations.Param;

public interface FavoriteMapper extends BaseMapper<Favorite> {
    Page<FavoriteListVO> getFavoriteList(Page<FavoriteListVO> page, @Param("userId") Long userId);
}
