package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.qingyuan.secondhand.service.ReviewService;
import com.qingyuan.secondhand.vo.ReviewDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final TradeOrderMapper tradeOrderMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void submitReview(ReviewSubmitDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (dto == null || dto.getOrderId() == null) {
            throw new BusinessException("订单ID不能为空");
        }
        TradeOrder order = tradeOrderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!userId.equals(order.getBuyerId()) && !userId.equals(order.getSellerId())) {
            throw new BusinessException("无权评价该订单");
        }
        if (!Integer.valueOf(3).equals(order.getStatus()) && !Integer.valueOf(4).equals(order.getStatus())) {
            throw new BusinessException("订单状态不正确");
        }
        if (order.getCompleteTime() == null) {
            throw new BusinessException("订单状态不正确");
        }
        LocalDateTime deadline = order.getCompleteTime().plusDays(7);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new BusinessException("评价窗口期已过");
        }
        Review existing = reviewMapper.selectOne(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, dto.getOrderId())
                .eq(Review::getReviewerId, userId));
        if (existing != null) {
            throw new BusinessException("您已评价过该订单");
        }
        Long targetId = userId.equals(order.getBuyerId()) ? order.getSellerId() : order.getBuyerId();
        Review review = new Review();
        review.setOrderId(dto.getOrderId());
        review.setReviewerId(userId);
        review.setTargetId(targetId);
        review.setScoreDesc(dto.getScoreDesc());
        review.setScoreAttitude(dto.getScoreAttitude());
        review.setScoreExperience(dto.getScoreExperience());
        review.setContent(dto.getContent());
        review.setIsAuto(0);
        int inserted = reviewMapper.insert(review);
        if (inserted <= 0) {
            throw new BusinessException("评价失败");
        }
        Long reviewCount = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, dto.getOrderId()));
        if (reviewCount != null && reviewCount >= 2 && !Integer.valueOf(4).equals(order.getStatus())) {
            TradeOrder updateOrder = new TradeOrder();
            updateOrder.setId(order.getId());
            updateOrder.setStatus(4);
            updateOrder.setUpdateTime(LocalDateTime.now());
            int updated = tradeOrderMapper.updateById(updateOrder);
            if (updated <= 0) {
                throw new BusinessException("更新订单状态失败");
            }
        }
        BigDecimal newScore = calculateUserScore(targetId);
        User updateUser = new User();
        updateUser.setId(targetId);
        updateUser.setScore(newScore);
        updateUser.setUpdateTime(LocalDateTime.now());
        int updatedUser = userMapper.updateById(updateUser);
        if (updatedUser <= 0) {
            throw new BusinessException("更新评分失败");
        }
        notificationService.send(
                targetId,
                10,
                "您收到了新的评价",
                "订单号：" + order.getOrderNo() + " 的交易对方给您评价了",
                order.getId(),
                2,
                1
        );
    }

    @Override
    public ReviewDetailVO getReviewDetail(Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (orderId == null) {
            throw new BusinessException("订单ID不能为空");
        }
        TradeOrder order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!userId.equals(order.getBuyerId()) && !userId.equals(order.getSellerId())) {
            throw new BusinessException("无权查看该订单评价");
        }
        if (!Integer.valueOf(3).equals(order.getStatus()) && !Integer.valueOf(4).equals(order.getStatus())) {
            throw new BusinessException("订单状态不正确");
        }
        List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, orderId));
        ReviewDetailVO vo = new ReviewDetailVO();
        vo.setOrderStatus(order.getStatus());
        if (reviews == null || reviews.isEmpty()) {
            return vo;
        }
        Review myReview = null;
        Review otherReview = null;
        for (Review review : reviews) {
            if (userId.equals(review.getReviewerId())) {
                myReview = review;
            } else {
                otherReview = review;
            }
        }
        fillMyReview(vo, myReview);
        if (Integer.valueOf(4).equals(order.getStatus())) {
            fillOtherReview(vo, otherReview);
        }
        return vo;
    }

    private BigDecimal calculateUserScore(Long targetId) {
        List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getTargetId, targetId));
        if (reviews == null || reviews.isEmpty()) {
            return BigDecimal.valueOf(5.0).setScale(1, RoundingMode.HALF_UP);
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Review review : reviews) {
            BigDecimal sum = BigDecimal.valueOf(review.getScoreDesc())
                    .add(BigDecimal.valueOf(review.getScoreAttitude()))
                    .add(BigDecimal.valueOf(review.getScoreExperience()));
            BigDecimal average = sum.divide(BigDecimal.valueOf(3), 4, RoundingMode.HALF_UP);
            total = total.add(average);
        }
        return total.divide(BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP);
    }

    private void fillMyReview(ReviewDetailVO vo, Review review) {
        if (review == null) {
            return;
        }
        vo.setMyReviewId(review.getId());
        vo.setMyScoreDesc(review.getScoreDesc());
        vo.setMyScoreAttitude(review.getScoreAttitude());
        vo.setMyScoreExperience(review.getScoreExperience());
        vo.setMyContent(review.getContent());
        vo.setMyCreateTime(review.getCreateTime());
    }

    private void fillOtherReview(ReviewDetailVO vo, Review review) {
        if (review == null) {
            return;
        }
        vo.setOtherReviewId(review.getId());
        vo.setOtherScoreDesc(review.getScoreDesc());
        vo.setOtherScoreAttitude(review.getScoreAttitude());
        vo.setOtherScoreExperience(review.getScoreExperience());
        vo.setOtherContent(review.getContent());
        vo.setOtherCreateTime(review.getCreateTime());
    }
}
