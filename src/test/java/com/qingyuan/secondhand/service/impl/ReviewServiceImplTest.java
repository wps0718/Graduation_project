package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.ReviewSubmitDTO;
import com.qingyuan.secondhand.entity.Review;
import com.qingyuan.secondhand.entity.TradeOrder;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.ReviewMapper;
import com.qingyuan.secondhand.mapper.TradeOrderMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.vo.ReviewDetailVO;
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
class ReviewServiceImplTest {

    @AfterEach
    void clearContext() {
        UserContext.removeCurrentUserId();
    }

    @Test
    void testSubmitReview_Success_FirstReview() {
        ReviewMapper reviewMapper = Mockito.mock(ReviewMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 3, LocalDateTime.now().minusDays(1));
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        Mockito.when(reviewMapper.selectOne(Mockito.any(LambdaQueryWrapper.class))).thenReturn(null);
        Mockito.when(reviewMapper.insert(Mockito.any(Review.class))).thenReturn(1);
        Mockito.when(reviewMapper.selectCount(Mockito.any(LambdaQueryWrapper.class))).thenReturn(1L);
        Mockito.when(reviewMapper.selectList(Mockito.any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(buildReview(1L, 10001L, 10002L, 5, 4, 3, "不错", 0)));

        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, tradeOrderMapper, userMapper, notificationService);
        ReviewSubmitDTO dto = buildSubmitDTO(1L, 5, 4, 3, "不错");

        service.submitReview(dto);

        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        Mockito.verify(reviewMapper).insert(reviewCaptor.capture());
        Review saved = reviewCaptor.getValue();
        Assertions.assertEquals(1L, saved.getOrderId());
        Assertions.assertEquals(10001L, saved.getReviewerId());
        Assertions.assertEquals(10002L, saved.getTargetId());
        Assertions.assertEquals(5, saved.getScoreDesc());
        Assertions.assertEquals(4, saved.getScoreAttitude());
        Assertions.assertEquals(3, saved.getScoreExperience());
        Assertions.assertEquals("不错", saved.getContent());
        Assertions.assertEquals(0, saved.getIsAuto());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userMapper).updateById(userCaptor.capture());
        Assertions.assertEquals(new BigDecimal("4.0"), userCaptor.getValue().getScore());
        Mockito.verify(notificationService).sendNotification(10002L, 10, "您收到新的评价");
        Mockito.verify(tradeOrderMapper, Mockito.never()).updateById(Mockito.any(TradeOrder.class));
    }

    @Test
    void testSubmitReview_Duplicate() {
        ReviewMapper reviewMapper = Mockito.mock(ReviewMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 3, LocalDateTime.now().minusDays(1));
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        Mockito.when(reviewMapper.selectOne(Mockito.any(LambdaQueryWrapper.class))).thenReturn(new Review());

        UserContext.setCurrentUserId(10001L);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, tradeOrderMapper, userMapper, notificationService);
        ReviewSubmitDTO dto = buildSubmitDTO(1L, 5, 5, 5, "好");

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.submitReview(dto));
        Assertions.assertEquals("您已评价过该订单", ex.getMsg());
    }

    @Test
    void testSubmitReview_Unauthorized() {
        ReviewMapper reviewMapper = Mockito.mock(ReviewMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 3, LocalDateTime.now().minusDays(1));
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        UserContext.setCurrentUserId(10003L);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, tradeOrderMapper, userMapper, notificationService);
        ReviewSubmitDTO dto = buildSubmitDTO(1L, 5, 5, 5, "好");

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.submitReview(dto));
        Assertions.assertEquals("无权评价该订单", ex.getMsg());
    }

    @Test
    void testSubmitReview_WrongStatus() {
        ReviewMapper reviewMapper = Mockito.mock(ReviewMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 1, LocalDateTime.now().minusDays(1));
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        UserContext.setCurrentUserId(10001L);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, tradeOrderMapper, userMapper, notificationService);
        ReviewSubmitDTO dto = buildSubmitDTO(1L, 5, 5, 5, "好");

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.submitReview(dto));
        Assertions.assertEquals("订单状态不正确", ex.getMsg());
    }

    @Test
    void testSubmitReview_Expired() {
        ReviewMapper reviewMapper = Mockito.mock(ReviewMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 3, LocalDateTime.now().minusDays(8));
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        UserContext.setCurrentUserId(10001L);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, tradeOrderMapper, userMapper, notificationService);
        ReviewSubmitDTO dto = buildSubmitDTO(1L, 5, 5, 5, "好");

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.submitReview(dto));
        Assertions.assertEquals("评价窗口期已过", ex.getMsg());
    }

    @Test
    void testSubmitReview_BothReviewed_UpdateStatus() {
        ReviewMapper reviewMapper = Mockito.mock(ReviewMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 3, LocalDateTime.now().minusDays(1));
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        Mockito.when(reviewMapper.selectOne(Mockito.any(LambdaQueryWrapper.class))).thenReturn(null);
        Mockito.when(reviewMapper.insert(Mockito.any(Review.class))).thenReturn(1);
        Mockito.when(reviewMapper.selectCount(Mockito.any(LambdaQueryWrapper.class))).thenReturn(2L);
        Mockito.when(reviewMapper.selectList(Mockito.any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(buildReview(1L, 10001L, 10002L, 5, 5, 5, null, 0)));

        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);
        Mockito.when(tradeOrderMapper.updateById(Mockito.any(TradeOrder.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, tradeOrderMapper, userMapper, notificationService);
        ReviewSubmitDTO dto = buildSubmitDTO(1L, 5, 5, 5, null);

        service.submitReview(dto);

        ArgumentCaptor<TradeOrder> orderCaptor = ArgumentCaptor.forClass(TradeOrder.class);
        Mockito.verify(tradeOrderMapper).updateById(orderCaptor.capture());
        Assertions.assertEquals(4, orderCaptor.getValue().getStatus());
    }

    @Test
    void testSubmitReview_FirstReviewDefaultScore() {
        ReviewMapper reviewMapper = Mockito.mock(ReviewMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 3, LocalDateTime.now().minusDays(1));
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        Mockito.when(reviewMapper.selectOne(Mockito.any(LambdaQueryWrapper.class))).thenReturn(null);
        Mockito.when(reviewMapper.insert(Mockito.any(Review.class))).thenReturn(1);
        Mockito.when(reviewMapper.selectCount(Mockito.any(LambdaQueryWrapper.class))).thenReturn(1L);
        Mockito.when(reviewMapper.selectList(Mockito.any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(buildReview(1L, 10001L, 10002L, 4, 5, 4, null, 0)));
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, tradeOrderMapper, userMapper, notificationService);
        ReviewSubmitDTO dto = buildSubmitDTO(1L, 4, 5, 4, "满意");

        service.submitReview(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userMapper).updateById(userCaptor.capture());
        Assertions.assertEquals(new BigDecimal("4.3"), userCaptor.getValue().getScore());

        Mockito.reset(reviewMapper, tradeOrderMapper, userMapper, notificationService);

        TradeOrder orderTwo = buildOrder(2L, 10001L, 10002L, 3, LocalDateTime.now().minusDays(1));
        Mockito.when(tradeOrderMapper.selectById(2L)).thenReturn(orderTwo);
        Mockito.when(reviewMapper.selectOne(Mockito.any(LambdaQueryWrapper.class))).thenReturn(null);
        Mockito.when(reviewMapper.insert(Mockito.any(Review.class))).thenReturn(1);
        Mockito.when(reviewMapper.selectCount(Mockito.any(LambdaQueryWrapper.class))).thenReturn(1L);
        Mockito.when(reviewMapper.selectList(Mockito.any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());
        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        ReviewServiceImpl serviceTwo = new ReviewServiceImpl(reviewMapper, tradeOrderMapper, userMapper, notificationService);
        ReviewSubmitDTO dtoTwo = buildSubmitDTO(2L, 5, 5, 5, "好评");

        serviceTwo.submitReview(dtoTwo);

        ArgumentCaptor<User> userCaptorTwo = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userMapper).updateById(userCaptorTwo.capture());
        Assertions.assertEquals(new BigDecimal("5.0"), userCaptorTwo.getValue().getScore());
    }

    @Test
    void testSubmitReview_ScoreRounding() {
        ReviewMapper reviewMapper = Mockito.mock(ReviewMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 3, LocalDateTime.now().minusDays(1));
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        Mockito.when(reviewMapper.selectOne(Mockito.any(LambdaQueryWrapper.class))).thenReturn(null);
        Mockito.when(reviewMapper.insert(Mockito.any(Review.class))).thenReturn(1);
        Mockito.when(reviewMapper.selectCount(Mockito.any(LambdaQueryWrapper.class))).thenReturn(1L);
        Mockito.when(reviewMapper.selectList(Mockito.any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(
                        buildReview(1L, 10001L, 10002L, 4, 4, 5, null, 0),
                        buildReview(2L, 10003L, 10002L, 5, 5, 5, null, 0)
                ));

        Mockito.when(userMapper.updateById(Mockito.any(User.class))).thenReturn(1);

        UserContext.setCurrentUserId(10001L);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, tradeOrderMapper, userMapper, notificationService);
        ReviewSubmitDTO dto = buildSubmitDTO(1L, 4, 4, 5, "满意");

        service.submitReview(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userMapper).updateById(userCaptor.capture());
        Assertions.assertEquals(new BigDecimal("4.7"), userCaptor.getValue().getScore());
    }

    @Test
    void testGetReviewDetail_Status3_OnlyMine() {
        ReviewMapper reviewMapper = Mockito.mock(ReviewMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 3, LocalDateTime.now().minusDays(1));
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        Mockito.when(reviewMapper.selectList(Mockito.any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(
                        buildReview(10L, 10001L, 10002L, 5, 5, 5, "好", 0),
                        buildReview(11L, 10002L, 10001L, 4, 4, 4, "一般", 0)
                ));

        UserContext.setCurrentUserId(10001L);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, tradeOrderMapper, userMapper, notificationService);
        ReviewDetailVO vo = service.getReviewDetail(1L);

        Assertions.assertEquals(3, vo.getOrderStatus());
        Assertions.assertEquals(10L, vo.getMyReviewId());
        Assertions.assertEquals(5, vo.getMyScoreDesc());
        Assertions.assertEquals("好", vo.getMyContent());
        Assertions.assertNull(vo.getOtherReviewId());
        Assertions.assertNull(vo.getOtherScoreDesc());
        Assertions.assertNull(vo.getOtherContent());
    }

    @Test
    void testGetReviewDetail_NoReview() {
        ReviewMapper reviewMapper = Mockito.mock(ReviewMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 3, LocalDateTime.now().minusDays(1));
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        Mockito.when(reviewMapper.selectList(Mockito.any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        UserContext.setCurrentUserId(10001L);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, tradeOrderMapper, userMapper, notificationService);
        ReviewDetailVO vo = service.getReviewDetail(1L);

        Assertions.assertEquals(3, vo.getOrderStatus());
        Assertions.assertNull(vo.getMyReviewId());
        Assertions.assertNull(vo.getOtherReviewId());
        Assertions.assertNull(vo.getMyContent());
        Assertions.assertNull(vo.getOtherContent());
    }

    @Test
    void testGetReviewDetail_Unauthorized() {
        ReviewMapper reviewMapper = Mockito.mock(ReviewMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 3, LocalDateTime.now().minusDays(1));
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);

        UserContext.setCurrentUserId(10003L);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, tradeOrderMapper, userMapper, notificationService);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.getReviewDetail(1L));
        Assertions.assertEquals("无权查看该订单评价", ex.getMsg());
    }

    @Test
    void testGetReviewDetail_Status4_Both() {
        ReviewMapper reviewMapper = Mockito.mock(ReviewMapper.class);
        TradeOrderMapper tradeOrderMapper = Mockito.mock(TradeOrderMapper.class);
        UserMapper userMapper = Mockito.mock(UserMapper.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        TradeOrder order = buildOrder(1L, 10001L, 10002L, 4, LocalDateTime.now().minusDays(1));
        Mockito.when(tradeOrderMapper.selectById(1L)).thenReturn(order);
        Mockito.when(reviewMapper.selectList(Mockito.any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(
                        buildReview(10L, 10001L, 10002L, 5, 5, 5, "好", 0),
                        buildReview(11L, 10002L, 10001L, 4, 4, 4, "一般", 1)
                ));

        UserContext.setCurrentUserId(10001L);
        ReviewServiceImpl service = new ReviewServiceImpl(reviewMapper, tradeOrderMapper, userMapper, notificationService);
        ReviewDetailVO vo = service.getReviewDetail(1L);

        Assertions.assertEquals(4, vo.getOrderStatus());
        Assertions.assertEquals(10L, vo.getMyReviewId());
        Assertions.assertEquals("好", vo.getMyContent());
        Assertions.assertEquals(11L, vo.getOtherReviewId());
        Assertions.assertEquals("一般", vo.getOtherContent());
    }

    private TradeOrder buildOrder(Long id, Long buyerId, Long sellerId, Integer status, LocalDateTime completeTime) {
        TradeOrder order = new TradeOrder();
        order.setId(id);
        order.setBuyerId(buyerId);
        order.setSellerId(sellerId);
        order.setStatus(status);
        order.setCompleteTime(completeTime);
        return order;
    }

    private ReviewSubmitDTO buildSubmitDTO(Long orderId, Integer scoreDesc, Integer scoreAttitude, Integer scoreExperience, String content) {
        ReviewSubmitDTO dto = new ReviewSubmitDTO();
        dto.setOrderId(orderId);
        dto.setScoreDesc(scoreDesc);
        dto.setScoreAttitude(scoreAttitude);
        dto.setScoreExperience(scoreExperience);
        dto.setContent(content);
        return dto;
    }

    private Review buildReview(Long id, Long reviewerId, Long targetId, Integer scoreDesc, Integer scoreAttitude, Integer scoreExperience, String content, Integer isAuto) {
        Review review = new Review();
        review.setId(id);
        review.setOrderId(1L);
        review.setReviewerId(reviewerId);
        review.setTargetId(targetId);
        review.setScoreDesc(scoreDesc);
        review.setScoreAttitude(scoreAttitude);
        review.setScoreExperience(scoreExperience);
        review.setContent(content);
        review.setIsAuto(isAuto);
        review.setCreateTime(LocalDateTime.now().minusHours(1));
        return review;
    }
}
