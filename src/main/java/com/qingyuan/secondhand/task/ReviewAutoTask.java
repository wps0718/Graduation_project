package com.qingyuan.secondhand.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingyuan.secondhand.common.enums.NotificationCategory;
import com.qingyuan.secondhand.common.enums.OrderStatus;
import com.qingyuan.secondhand.entity.Review;
import com.qingyuan.secondhand.entity.TradeOrder;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.ReviewMapper;
import com.qingyuan.secondhand.mapper.TradeOrderMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "task.enabled.review-auto", havingValue = "true", matchIfMissing = true)
public class ReviewAutoTask {

    private final TradeOrderMapper tradeOrderMapper;
    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void execute() {
        LocalDateTime start = LocalDateTime.now();
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        log.info("[自动好评任务] 开始执行，时间：{}", start);
        try {
            LocalDateTime deadline = LocalDateTime.now().minusDays(7);
            LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TradeOrder::getStatus, OrderStatus.COMPLETED.getCode())
                    .lt(TradeOrder::getCompleteTime, deadline);
            List<TradeOrder> orders = tradeOrderMapper.selectList(wrapper);
            if (orders == null || orders.isEmpty()) {
                log.info("[自动好评任务] 无需自动好评的订单");
                return;
            }
            for (TradeOrder order : orders) {
                try {
                    List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                            .eq(Review::getOrderId, order.getId()));
                    boolean buyerReviewed = hasReviewed(reviews, order.getBuyerId());
                    boolean sellerReviewed = hasReviewed(reviews, order.getSellerId());
                    int newCount = 0;
                    Set<Long> targetIds = new HashSet<>();

                    if (!buyerReviewed) {
                        Review review = buildAutoReview(order.getId(), order.getBuyerId(), order.getSellerId());
                        int inserted = reviewMapper.insert(review);
                        if (inserted <= 0) {
                            throw new RuntimeException("自动好评失败");
                        }
                        notificationService.send(
                                order.getSellerId(),
                                10,
                                "收到评价",
                                "您收到了一条系统自动好评",
                                order.getId(),
                                2,
                                NotificationCategory.TRANSACTION.getCode()
                        );
                        targetIds.add(order.getSellerId());
                        newCount++;
                    }
                    if (!sellerReviewed) {
                        Review review = buildAutoReview(order.getId(), order.getSellerId(), order.getBuyerId());
                        int inserted = reviewMapper.insert(review);
                        if (inserted <= 0) {
                            throw new RuntimeException("自动好评失败");
                        }
                        notificationService.send(
                                order.getBuyerId(),
                                10,
                                "收到评价",
                                "您收到了一条系统自动好评",
                                order.getId(),
                                2,
                                NotificationCategory.TRANSACTION.getCode()
                        );
                        targetIds.add(order.getBuyerId());
                        newCount++;
                    }

                    int totalReviews = (reviews == null ? 0 : reviews.size()) + newCount;
                    if (totalReviews >= 2 && !OrderStatus.RATED.getCode().equals(order.getStatus())) {
                        TradeOrder update = new TradeOrder();
                        update.setId(order.getId());
                        update.setStatus(OrderStatus.RATED.getCode());
                        update.setUpdateTime(LocalDateTime.now());
                        tradeOrderMapper.updateById(update);
                    }

                    for (Long targetId : targetIds) {
                        BigDecimal score = calculateUserScore(targetId);
                        User updateUser = new User();
                        updateUser.setId(targetId);
                        updateUser.setScore(score);
                        updateUser.setUpdateTime(LocalDateTime.now());
                        userMapper.updateById(updateUser);
                    }
                    if (newCount > 0) {
                        processedCount++;
                    }
                } catch (Exception e) {
                    log.error("[自动好评任务] 处理订单失败，订单ID：{}，错误：{}", order.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("[自动好评任务] 执行失败：{}", e.getMessage(), e);
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("[自动好评任务] 执行完成，处理条数：{}，耗时：{}ms", processedCount, endTime - startTime);
        }
    }

    private boolean hasReviewed(List<Review> reviews, Long reviewerId) {
        if (reviews == null || reviews.isEmpty()) {
            return false;
        }
        for (Review review : reviews) {
            if (reviewerId.equals(review.getReviewerId())) {
                return true;
            }
        }
        return false;
    }

    private Review buildAutoReview(Long orderId, Long reviewerId, Long targetId) {
        Review review = new Review();
        review.setOrderId(orderId);
        review.setReviewerId(reviewerId);
        review.setTargetId(targetId);
        review.setScoreDesc(5);
        review.setScoreAttitude(5);
        review.setScoreExperience(5);
        review.setContent("系统自动好评");
        review.setIsAuto(1);
        return review;
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
}
