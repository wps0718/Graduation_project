package com.qingyuan.secondhand.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingyuan.secondhand.common.enums.NotificationCategory;
import com.qingyuan.secondhand.common.enums.NotificationType;
import com.qingyuan.secondhand.common.enums.OrderStatus;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.entity.Review;
import com.qingyuan.secondhand.entity.TradeOrder;
import com.qingyuan.secondhand.mapper.ProductMapper;
import com.qingyuan.secondhand.mapper.ReviewMapper;
import com.qingyuan.secondhand.mapper.TradeOrderMapper;
import com.qingyuan.secondhand.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "task.enabled.review-remind", havingValue = "true", matchIfMissing = true)
public class ReviewRemindTask {

    private final TradeOrderMapper tradeOrderMapper;
    private final ReviewMapper reviewMapper;
    private final ProductMapper productMapper;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 10 * * ?")
    public void execute() {
        LocalDateTime start = LocalDateTime.now();
        long startTime = System.currentTimeMillis();
        int remindCount = 0;
        log.info("[评价提醒任务] 开始执行，时间：{}", start);
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime threeDaysAgo = now.minusDays(3);
            LocalDateTime fourDaysAgo = now.minusDays(4);
            LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TradeOrder::getStatus, OrderStatus.COMPLETED.getCode())
                    .between(TradeOrder::getCompleteTime, fourDaysAgo, threeDaysAgo);
            List<TradeOrder> orders = tradeOrderMapper.selectList(wrapper);
            if (orders == null || orders.isEmpty()) {
                log.info("[评价提醒任务] 无需提醒的订单");
                return;
            }
            for (TradeOrder order : orders) {
                try {
                    List<Review> reviews = reviewMapper.selectList(
                            new LambdaQueryWrapper<Review>()
                                    .eq(Review::getOrderId, order.getId())
                    );
                    boolean buyerReviewed = hasReviewed(reviews, order.getBuyerId());
                    boolean sellerReviewed = hasReviewed(reviews, order.getSellerId());
                    Product product = productMapper.selectById(order.getProductId());
                    String productName = product != null && StringUtils.hasText(product.getTitle()) ? product.getTitle() : "商品";
                    Map<String, String> params = Map.of("productName", productName);

                    if (!buyerReviewed) {
                        notificationService.send(
                                order.getBuyerId(),
                                NotificationType.REVIEW_REMIND,
                                params,
                                order.getId(),
                                2,
                                NotificationCategory.TRANSACTION.getCode()
                        );
                        remindCount++;
                    }
                    if (!sellerReviewed) {
                        notificationService.send(
                                order.getSellerId(),
                                NotificationType.REVIEW_REMIND,
                                params,
                                order.getId(),
                                2,
                                NotificationCategory.TRANSACTION.getCode()
                        );
                        remindCount++;
                    }
                } catch (Exception e) {
                    log.error("[评价提醒任务] 处理订单失败，订单ID：{}，错误：{}", order.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("[评价提醒任务] 执行失败：{}", e.getMessage(), e);
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("[评价提醒任务] 执行完成，提醒条数：{}，耗时：{}ms", remindCount, endTime - startTime);
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
}
