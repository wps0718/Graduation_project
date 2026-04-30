package com.qingyuan.secondhand.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.enums.NotificationCategory;
import com.qingyuan.secondhand.common.enums.OrderStatus;
import com.qingyuan.secondhand.common.enums.ProductStatus;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.entity.TradeOrder;
import com.qingyuan.secondhand.mapper.ProductMapper;
import com.qingyuan.secondhand.mapper.TradeOrderMapper;
import com.qingyuan.secondhand.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "task.enabled.order-auto-confirm", havingValue = "true", matchIfMissing = true)
public class OrderAutoConfirmTask {

    private final TradeOrderMapper tradeOrderMapper;
    private final ProductMapper productMapper;
    private final NotificationService notificationService;
    private final StringRedisTemplate stringRedisTemplate;

    @Scheduled(cron = "0 0 2 * * ?")
    public void execute() {
        LocalDateTime start = LocalDateTime.now();
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        log.info("[订单自动确认任务] 开始执行，时间：{}", start);
        try {
            LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(TradeOrder::getConfirmDeadline, LocalDateTime.now())
                    .eq(TradeOrder::getStatus, OrderStatus.PENDING_MEET.getCode());
            List<TradeOrder> orders = tradeOrderMapper.selectList(wrapper);
            if (orders == null || orders.isEmpty()) {
                log.info("[订单自动确认任务] 无需自动确认的订单");
                return;
            }
            for (TradeOrder order : orders) {
                try {
                    TradeOrder update = new TradeOrder();
                    update.setId(order.getId());
                    update.setStatus(OrderStatus.COMPLETED.getCode());
                    update.setCompleteTime(LocalDateTime.now());
                    update.setUpdateTime(LocalDateTime.now());
                    tradeOrderMapper.updateById(update);

                    Product product = productMapper.selectById(order.getProductId());
                    if (product != null && Integer.valueOf(0).equals(product.getIsDeleted())) {
                        Product productUpdate = new Product();
                        productUpdate.setId(product.getId());
                        productUpdate.setStatus(ProductStatus.SOLD.getCode());
                        productUpdate.setUpdateTime(LocalDateTime.now());
                        productMapper.updateById(productUpdate);

                        stringRedisTemplate.delete(RedisConstant.USER_STATS + product.getUserId());
                    }

                    notificationService.send(
                            order.getBuyerId(),
                            1,
                            "订单已确认",
                            "订单已超过确认期限，系统已自动确认收货",
                            order.getId(),
                            2,
                            NotificationCategory.TRANSACTION.getCode()
                    );
                    notificationService.send(
                            order.getSellerId(),
                            1,
                            "订单已确认",
                            "订单已超过确认期限，系统已自动确认收货",
                            order.getId(),
                            2,
                            NotificationCategory.TRANSACTION.getCode()
                    );
                    processedCount++;
                } catch (Exception e) {
                    log.error("[订单自动确认任务] 处理订单失败，订单ID：{}，错误：{}", order.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("[订单自动确认任务] 执行失败：{}", e.getMessage(), e);
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("[订单自动确认任务] 执行完成，处理条数：{}，耗时：{}ms", processedCount, endTime - startTime);
        }
    }
}
