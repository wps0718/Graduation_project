package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.entity.Favorite;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.mapper.FavoriteMapper;
import com.qingyuan.secondhand.mapper.ProductMapper;
import com.qingyuan.secondhand.service.FavoriteService;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.vo.FavoriteListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @Override
    public void addFavorite(Long productId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (productId == null) {
            throw new BusinessException("商品ID不能为空");
        }
        Product product = productMapper.selectById(productId);
        if (product == null || Integer.valueOf(1).equals(product.getIsDeleted())) {
            throw new BusinessException("商品不存在");
        }
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        if (count != null && count > 0) {
            throw new BusinessException("已收藏该商品");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        try {
            int inserted = favoriteMapper.insert(favorite);
            if (inserted <= 0) {
                throw new BusinessException("收藏失败");
            }
        } catch (DuplicateKeyException ex) {
            throw new BusinessException("已收藏该商品");
        }

        int existingCount = product.getFavoriteCount() == null ? 0 : product.getFavoriteCount();
        Product update = new Product();
        update.setId(productId);
        update.setFavoriteCount(existingCount + 1);
        int updated = productMapper.updateById(update);
        if (updated <= 0) {
            throw new BusinessException("收藏失败");
        }
        notificationService.sendNotification(product.getUserId(), 6, "您的商品被收藏");
    }

    @Override
    public void cancelFavorite(Long productId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (productId == null) {
            throw new BusinessException("商品ID不能为空");
        }
        Favorite favorite = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        if (favorite == null) {
            throw new BusinessException("未收藏该商品");
        }
        int deleted = favoriteMapper.deleteById(favorite.getId());
        if (deleted <= 0) {
            throw new BusinessException("取消收藏失败");
        }
        Product product = productMapper.selectById(productId);
        Integer favoriteCount = product == null ? null : product.getFavoriteCount();
        if (favoriteCount != null && favoriteCount > 0) {
            Product update = new Product();
            update.setId(productId);
            update.setFavoriteCount(favoriteCount - 1);
            int updated = productMapper.updateById(update);
            if (updated <= 0) {
                throw new BusinessException("取消收藏失败");
            }
        }
    }

    @Override
    public IPage<FavoriteListVO> getFavoriteList(Integer page, Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (page == null || pageSize == null) {
            throw new BusinessException("分页参数不能为空");
        }
        Page<FavoriteListVO> pageResult = favoriteMapper.getFavoriteList(new Page<>(page, pageSize), userId);
        if (pageResult == null) {
            return new Page<>(page, pageSize, 0);
        }
        List<FavoriteListVO> records = pageResult.getRecords();
        if (records != null) {
            records.forEach(item -> item.setCoverImage(parseCoverImage(item.getCoverImage())));
        }
        return pageResult;
    }

    @Override
    public boolean checkFavorite(Long productId) {
        if (productId == null) {
            throw new BusinessException("商品ID不能为空");
        }
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return false;
        }
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        return count != null && count > 0;
    }

    private String parseCoverImage(String imagesJson) {
        if (!StringUtils.hasText(imagesJson)) {
            return null;
        }
        try {
            List<String> images = objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {
            });
            if (images == null || images.isEmpty()) {
                return null;
            }
            return images.get(0);
        } catch (Exception e) {
            return null;
        }
    }
}
