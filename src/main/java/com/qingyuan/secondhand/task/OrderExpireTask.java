package com.qingyuan.secondhand.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "task.enabled.order-expire", havingValue = "true", matchIfMissing = true)
public class OrderExpireTask {

    private final TradeOrderMapper tradeOrderMapper;
    private final ProductMapper productMapper;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void execute() {
        LocalDateTime start = LocalDateTime.now();
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        log.info("[订单超时取消任务] 开始执行，时间：{}", start);
        try {
            LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(TradeOrder::getExpireTime, LocalDateTime.now())
                    .eq(TradeOrder::getStatus, OrderStatus.PENDING_MEET.getCode());
            List<TradeOrder> orders = tradeOrderMapper.selectList(wrapper);
            if (orders == null || orders.isEmpty()) {
                log.info("[订单超时取消任务] 无超时订单");
                return;
            }
            for (TradeOrder order : orders) {
                try {
                    TradeOrder update = new TradeOrder();
                    update.setId(order.getId());
                    update.setStatus(OrderStatus.CANCELLED.getCode());
                    update.setCancelBy(0L);
                    update.setCancelReason("订单超时未面交，系统自动取消");
                    update.setUpdateTime(LocalDateTime.now());
                    tradeOrderMapper.updateById(update);

                    Product product = productMapper.selectById(order.getProductId());
                    if (product != null && Integer.valueOf(0).equals(product.getIsDeleted())) {
                        if (ProductStatus.ON_SALE.getCode().equals(product.getStatus())) {
                            // 商品状态正常为在售，无需变更
                            log.info("订单[{}]超时取消，商品[{}]状态正常在售", order.getId(), product.getId());
                        } else {
                            // 商品状态已被变更（如管理员强制下架），记录警告但跳过
                            log.warn("订单[{}]超时取消时商品[{}]状态已变更为{}，跳过状态同步",
                                     order.getId(), product.getId(), product.getStatus());
                        }
                    }

                    notificationService.send(
                            order.getBuyerId(),
                            1,
                            "订单已取消",
                            "您的订单已超时，系统已自动取消",
                            order.getId(),
                            2,
                            NotificationCategory.TRANSACTION.getCode()
                    );
                    notificationService.send(
                            order.getSellerId(),
                            1,
                            "订单已取消",
                            "买家订单已超时，系统已自动取消",
                            order.getId(),
                            2,
                            NotificationCategory.TRANSACTION.getCode()
                    );
                    processedCount++;
                } catch (Exception e) {
                    log.error("[订单超时取消任务] 处理订单失败，订单ID：{}，错误：{}", order.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("[订单超时取消任务] 执行失败：{}", e.getMessage(), e);
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("[订单超时取消任务] 执行完成，处理条数：{}，耗时：{}ms", processedCount, endTime - startTime);
        }
    }
}
