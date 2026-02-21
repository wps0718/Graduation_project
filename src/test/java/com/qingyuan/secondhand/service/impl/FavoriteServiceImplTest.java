package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.entity.Favorite;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.mapper.FavoriteMapper;
import com.qingyuan.secondhand.mapper.ProductMapper;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.vo.FavoriteListVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @AfterEach
    void clearContext() {
        UserContext.removeCurrentUserId();
    }

    @Test
    void testAddFavorite_Success() {
        FavoriteMapper favoriteMapper = Mockito.mock(FavoriteMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Product product = new Product();
        product.setId(1L);
        product.setUserId(10002L);
        product.setFavoriteCount(5);
        product.setIsDeleted(0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(product);
        Mockito.when(favoriteMapper.selectCount(Mockito.any())).thenReturn(0L);
        Mockito.when(favoriteMapper.insert(Mockito.<Favorite>any())).thenReturn(1);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        FavoriteServiceImpl service = new FavoriteServiceImpl(favoriteMapper, productMapper, objectMapper, notificationService);
        service.addFavorite(1L);

        ArgumentCaptor<Favorite> favoriteCaptor = ArgumentCaptor.forClass(Favorite.class);
        Mockito.verify(favoriteMapper).insert(favoriteCaptor.capture());
        Favorite saved = favoriteCaptor.getValue();
        Assertions.assertEquals(10001L, saved.getUserId());
        Assertions.assertEquals(1L, saved.getProductId());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).updateById(productCaptor.capture());
        Assertions.assertEquals(6, productCaptor.getValue().getFavoriteCount());
        Mockito.verify(notificationService).sendNotification(10002L, 6, "您的商品被收藏");
    }

    @Test
    void testAddFavorite_ProductNotExist() {
        FavoriteMapper favoriteMapper = Mockito.mock(FavoriteMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Mockito.when(productMapper.selectById(1L)).thenReturn(null);
        UserContext.setCurrentUserId(10001L);
        FavoriteServiceImpl service = new FavoriteServiceImpl(favoriteMapper, productMapper, objectMapper, notificationService);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.addFavorite(1L));
        Assertions.assertEquals("商品不存在", ex.getMsg());
    }

    @Test
    void testAddFavorite_AlreadyFavorited() {
        FavoriteMapper favoriteMapper = Mockito.mock(FavoriteMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Product product = new Product();
        product.setId(1L);
        product.setUserId(10002L);
        product.setFavoriteCount(1);
        product.setIsDeleted(0);
        Mockito.when(productMapper.selectById(1L)).thenReturn(product);
        Mockito.when(favoriteMapper.selectCount(Mockito.any())).thenReturn(1L);

        UserContext.setCurrentUserId(10001L);
        FavoriteServiceImpl service = new FavoriteServiceImpl(favoriteMapper, productMapper, objectMapper, notificationService);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.addFavorite(1L));
        Assertions.assertEquals("已收藏该商品", ex.getMsg());
        Mockito.verify(favoriteMapper, Mockito.never()).insert(Mockito.<Favorite>any());
    }

    @Test
    void testCancelFavorite_Success() {
        FavoriteMapper favoriteMapper = Mockito.mock(FavoriteMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Favorite favorite = new Favorite();
        favorite.setId(100L);
        favorite.setUserId(10001L);
        favorite.setProductId(1L);
        Mockito.when(favoriteMapper.selectOne(Mockito.any())).thenReturn(favorite);
        Mockito.when(favoriteMapper.deleteById(100L)).thenReturn(1);

        Product product = new Product();
        product.setId(1L);
        product.setFavoriteCount(5);
        Mockito.when(productMapper.selectById(1L)).thenReturn(product);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        FavoriteServiceImpl service = new FavoriteServiceImpl(favoriteMapper, productMapper, objectMapper, notificationService);
        service.cancelFavorite(1L);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).updateById(productCaptor.capture());
        Assertions.assertEquals(4, productCaptor.getValue().getFavoriteCount());
        Mockito.verify(favoriteMapper).deleteById(100L);
    }

    @Test
    void testCancelFavorite_NotFavorited() {
        FavoriteMapper favoriteMapper = Mockito.mock(FavoriteMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Mockito.when(favoriteMapper.selectOne(Mockito.any())).thenReturn(null);
        UserContext.setCurrentUserId(10001L);
        FavoriteServiceImpl service = new FavoriteServiceImpl(favoriteMapper, productMapper, objectMapper, notificationService);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.cancelFavorite(1L));
        Assertions.assertEquals("未收藏该商品", ex.getMsg());
    }

    @Test
    void testGetFavoriteList_Success() throws Exception {
        FavoriteMapper favoriteMapper = Mockito.mock(FavoriteMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        FavoriteListVO vo = new FavoriteListVO();
        vo.setFavoriteId(1L);
        vo.setProductId(2L);
        vo.setFavoriteTime(LocalDateTime.now());
        vo.setTitle("商品A");
        vo.setPrice(new BigDecimal("9.9"));
        vo.setCoverImage("[\"img1\",\"img2\"]");
        vo.setStatus(1);
        vo.setConditionLevel(2);
        vo.setCampusName("南海北");
        vo.setSellerId(10002L);
        vo.setSellerNickName("卖家");

        Page<FavoriteListVO> page = new Page<>(1, 10, 1);
        page.setRecords(List.of(vo));
        Mockito.when(favoriteMapper.getFavoriteList(Mockito.any(Page.class), Mockito.eq(10001L))).thenReturn(page);
        Mockito.when(objectMapper.readValue(Mockito.anyString(), Mockito.any(TypeReference.class)))
                .thenReturn(List.of("img1", "img2"));

        UserContext.setCurrentUserId(10001L);
        FavoriteServiceImpl service = new FavoriteServiceImpl(favoriteMapper, productMapper, objectMapper, notificationService);
        IPage<FavoriteListVO> result = service.getFavoriteList(1, 10);

        Assertions.assertEquals(1, result.getTotal());
        Assertions.assertEquals("img1", result.getRecords().get(0).getCoverImage());
    }

    @Test
    void testCheckFavorite_True() {
        FavoriteMapper favoriteMapper = Mockito.mock(FavoriteMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Mockito.when(favoriteMapper.selectCount(Mockito.any())).thenReturn(1L);
        UserContext.setCurrentUserId(10001L);
        FavoriteServiceImpl service = new FavoriteServiceImpl(favoriteMapper, productMapper, objectMapper, notificationService);

        Assertions.assertTrue(service.checkFavorite(1L));
    }

    @Test
    void testCheckFavorite_False() {
        FavoriteMapper favoriteMapper = Mockito.mock(FavoriteMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Mockito.when(favoriteMapper.selectCount(Mockito.any())).thenReturn(0L);
        UserContext.setCurrentUserId(10001L);
        FavoriteServiceImpl service = new FavoriteServiceImpl(favoriteMapper, productMapper, objectMapper, notificationService);

        Assertions.assertFalse(service.checkFavorite(1L));
    }
}
