package com.qingyuan.secondhand.task;

import com.qingyuan.secondhand.common.enums.NotificationType;
import com.qingyuan.secondhand.common.enums.OrderStatus;
import com.qingyuan.secondhand.common.enums.ProductStatus;
import com.qingyuan.secondhand.common.enums.UserStatus;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.entity.Review;
import com.qingyuan.secondhand.entity.TradeOrder;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.ProductMapper;
import com.qingyuan.secondhand.mapper.ReviewMapper;
import com.qingyuan.secondhand.mapper.TradeOrderMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.NotificationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class ScheduledTaskTest {

    @Test
    void testOrderExpireTask() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        OrderExpireTask task = new OrderExpireTask(tradeOrderMapper, productMapper, notificationService);

        TradeOrder order = buildOrder(1L, 11L, 21L, 31L, OrderStatus.PENDING_MEET.getCode());
        order.setExpireTime(LocalDateTime.now().minusHours(1));
        Product product = buildProduct(11L, 31L, ProductStatus.ON_SALE.getCode(), 0);

        Mockito.when(tradeOrderMapper.selectList(Mockito.any())).thenReturn(List.of(order));
        Mockito.when(productMapper.selectById(11L)).thenReturn(product);
        Mockito.when(tradeOrderMapper.updateById(Mockito.any(TradeOrder.class))).thenReturn(1);

        task.execute();

        ArgumentCaptor<TradeOrder> orderCaptor = ArgumentCaptor.forClass(TradeOrder.class);
        Mockito.verify(tradeOrderMapper).updateById(orderCaptor.capture());
        TradeOrder updatedOrder = orderCaptor.getValue();
        Assertions.assertEquals(OrderStatus.CANCELLED.getCode(), updatedOrder.getStatus());
        Assertions.assertEquals(0L, updatedOrder.getCancelBy());
        Assertions.assertEquals("订单超时未面交，系统自动取消", updatedOrder.getCancelReason());

        // 商品状态正常为在售时，超时取消不再覆盖写入（修复：尊重管理员操作）
        Mockito.verify(productMapper, Mockito.never()).updateById(Mockito.any(Product.class));

        Mockito.verify(notificationService, Mockito.times(2))
                .send(Mockito.anyLong(), Mockito.eq(1), Mockito.anyString(), Mockito.anyString(), Mockito.eq(1L), Mockito.eq(2), Mockito.eq(1));
    }

    @Test
    void testOrderAutoConfirmTask() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        OrderAutoConfirmTask task = new OrderAutoConfirmTask(tradeOrderMapper, productMapper, notificationService, stringRedisTemplate);

        TradeOrder order = buildOrder(2L, 12L, 22L, 32L, OrderStatus.PENDING_MEET.getCode());
        order.setConfirmDeadline(LocalDateTime.now().minusDays(1));
        Product product = buildProduct(12L, 32L, ProductStatus.ON_SALE.getCode(), 0);

        Mockito.when(tradeOrderMapper.selectList(Mockito.any())).thenReturn(List.of(order));
        Mockito.when(productMapper.selectById(12L)).thenReturn(product);
        Mockito.when(tradeOrderMapper.updateById(Mockito.any(TradeOrder.class))).thenReturn(1);
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        task.execute();

        ArgumentCaptor<TradeOrder> orderCaptor = ArgumentCaptor.forClass(TradeOrder.class);
        Mockito.verify(tradeOrderMapper).updateById(orderCaptor.capture());
        TradeOrder updatedOrder = orderCaptor.getValue();
        Assertions.assertEquals(OrderStatus.COMPLETED.getCode(), updatedOrder.getStatus());
        Assertions.assertNotNull(updatedOrder.getCompleteTime());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).updateById(productCaptor.capture());
        Product updatedProduct = productCaptor.getValue();
        Assertions.assertEquals(ProductStatus.SOLD.getCode(), updatedProduct.getStatus());

        Mockito.verify(notificationService, Mockito.times(2))
                .send(Mockito.anyLong(), Mockito.eq(1), Mockito.anyString(), Mockito.anyString(), Mockito.eq(2L), Mockito.eq(2), Mockito.eq(1));
    }

    @Test
    void testReviewAutoTask() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ReviewMapper reviewMapper = Mockito.mock(ReviewMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        ReviewAutoTask task = new ReviewAutoTask(tradeOrderMapper, reviewMapper, userMapper);

        TradeOrder order = buildOrder(3L, 13L, 23L, 33L, OrderStatus.COMPLETED.getCode());
        order.setCompleteTime(LocalDateTime.now().minusDays(8));

        Mockito.when(tradeOrderMapper.selectList(Mockito.any())).thenReturn(List.of(order));
        Mockito.when(reviewMapper.selectList(Mockito.any())).thenReturn(List.of());
        Mockito.when(reviewMapper.insert(Mockito.any(Review.class))).thenReturn(1);
        Mockito.when(tradeOrderMapper.updateById(Mockito.any(TradeOrder.class))).thenReturn(1);
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);

        task.execute();

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        Mockito.verify(reviewMapper, Mockito.times(2)).insert(reviewCaptor.capture());
        List<Review> reviews = reviewCaptor.getAllValues();
        Assertions.assertEquals(2, reviews.size());
        for (Review review : reviews) {
            Assertions.assertEquals(5, review.getScoreDesc());
            Assertions.assertEquals(5, review.getScoreAttitude());
            Assertions.assertEquals(5, review.getScoreExperience());
            Assertions.assertEquals("系统自动好评", review.getContent());
            Assertions.assertEquals(1, review.getIsAuto());
        }

        ArgumentCaptor<TradeOrder> orderCaptor = ArgumentCaptor.forClass(TradeOrder.class);
        Mockito.verify(tradeOrderMapper).updateById(orderCaptor.capture());
        Assertions.assertEquals(OrderStatus.RATED.getCode(), orderCaptor.getValue().getStatus());

        Mockito.verify(userMapper, Mockito.times(2)).updateById(Mockito.any(User.class));
    }

    @Test
    void testReviewRemindTask() {
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        ReviewMapper reviewMapper = Mockito.mock(ReviewMapper.class);
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        ReviewRemindTask task = new ReviewRemindTask(tradeOrderMapper, reviewMapper, productMapper, notificationService);

        TradeOrder order = buildOrder(4L, 14L, 24L, 34L, OrderStatus.COMPLETED.getCode());
        order.setCompleteTime(LocalDateTime.now().minusDays(3).minusHours(1));
        Product product = buildProduct(14L, 34L, ProductStatus.SOLD.getCode(), 0);

        Mockito.when(tradeOrderMapper.selectList(Mockito.any())).thenReturn(List.of(order));
        Mockito.when(reviewMapper.selectList(Mockito.any())).thenReturn(List.of());
        Mockito.when(productMapper.selectById(14L)).thenReturn(product);

        task.execute();

        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        Mockito.verify(notificationService, Mockito.times(2))
                .send(userIdCaptor.capture(),
                        Mockito.eq(NotificationType.REVIEW_REMIND),
                        Mockito.eq(Map.of("productName", "测试商品")),
                        Mockito.eq(4L),
                        Mockito.eq(2),
                        Mockito.eq(1));
        List<Long> userIds = userIdCaptor.getAllValues();
        Assertions.assertTrue(userIds.contains(24L));
        Assertions.assertTrue(userIds.contains(34L));
    }

    @Test
    void testProductAutoOffTask() {
        ProductMapper productMapper = Mockito.mock(ProductMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ProductAutoOffTask task = new ProductAutoOffTask(productMapper, notificationService, stringRedisTemplate);

        Product product = buildProduct(14L, 34L, ProductStatus.ON_SALE.getCode(), 0);
        product.setAutoOffTime(LocalDateTime.now().minusDays(1));

        Mockito.when(productMapper.selectList(Mockito.any())).thenReturn(List.of(product));
        Mockito.when(productMapper.updateById(Mockito.any(Product.class))).thenReturn(1);

        task.execute();

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        Mockito.verify(productMapper).updateById(productCaptor.capture());
        Product updatedProduct = productCaptor.getValue();
        Assertions.assertEquals(ProductStatus.OFF_SHELF.getCode(), updatedProduct.getStatus());

        Mockito.verify(notificationService)
                .send(Mockito.eq(34L), Mockito.eq(3), Mockito.anyString(), Mockito.anyString(), Mockito.eq(14L), Mockito.eq(1), Mockito.eq(2));
    }

    @Test
    void testUserDeactivateTask() {
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        UserDeactivateTask task = new UserDeactivateTask(userMapper);

        User user = new User();
        user.setId(40L);
        user.setStatus(UserStatus.DEREGISTERING.getCode());
        user.setDeactivateTime(LocalDateTime.now().minusDays(40));

        Mockito.when(userMapper.selectList(Mockito.any())).thenReturn(List.of(user));
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);

        task.execute();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userMapper).updateById(userCaptor.capture());
        User updatedUser = userCaptor.getValue();
        Assertions.assertEquals("已注销用户", updatedUser.getNickName());
        Assertions.assertEquals("", updatedUser.getAvatarUrl());
        Assertions.assertNull(updatedUser.getPhone());
        Assertions.assertNull(updatedUser.getOpenId());
        Assertions.assertNull(updatedUser.getSessionKey());
        Assertions.assertEquals(UserStatus.BANNED.getCode(), updatedUser.getStatus());
        Assertions.assertNull(updatedUser.getDeactivateTime());
    }

    private TradeOrder buildOrder(Long id, Long productId, Long buyerId, Long sellerId, Integer status) {
        TradeOrder order = new TradeOrder();
        order.setId(id);
        order.setProductId(productId);
        order.setBuyerId(buyerId);
        order.setSellerId(sellerId);
        order.setStatus(status);
        return order;
    }

    private Product buildProduct(Long id, Long userId, Integer status, Integer isDeleted) {
        Product product = new Product();
        product.setId(id);
        product.setUserId(userId);
        product.setStatus(status);
        product.setIsDeleted(isDeleted);
        product.setTitle("测试商品");
        return product;
    }
}
