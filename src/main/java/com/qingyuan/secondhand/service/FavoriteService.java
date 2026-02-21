package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.entity.Favorite;
import com.qingyuan.secondhand.vo.FavoriteListVO;

public interface FavoriteService extends IService<Favorite> {
    void addFavorite(Long productId);

    void cancelFavorite(Long productId);

    IPage<FavoriteListVO> getFavoriteList(Integer page, Integer pageSize);

    boolean checkFavorite(Long productId);
}
