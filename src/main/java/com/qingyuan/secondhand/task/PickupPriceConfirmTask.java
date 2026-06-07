package com.qingyuan.secondhand.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingyuan.secondhand.common.enums.NotificationCategory;
import com.qingyuan.secondhand.common.enums.NotificationType;
import com.qingyuan.secondhand.common.enums.PickupOrderStatus;
import com.qingyuan.secondhand.entity.PickupOrder;
import com.qingyuan.secondhand.mapper.PickupOrderMapper;
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
@ConditionalOnProperty(name = "task.enabled.pickup-price-confirm", havingValue = "true", matchIfMissing = true)
public class PickupPriceConfirmTask {

    private final PickupOrderMapper pickupOrderMapper;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void execute() {
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        log.info("[代拿价格确认超时任务] 开始执行");
        try {
            LambdaQueryWrapper<PickupOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(PickupOrder::getPriceDeadline, LocalDateTime.now())
                    .eq(PickupOrder::getStatus, PickupOrderStatus.ACCEPTED.getCode())
                    .eq(PickupOrder::getPickerPriceConfirmed, 0)
                    .last("LIMIT 500");
            List<PickupOrder> orders = pickupOrderMapper.selectList(wrapper);
            if (orders == null || orders.isEmpty()) {
                log.info("[代拿价格确认超时任务] 无超时订单");
                return;
            }

            for (PickupOrder order : orders) {
                try {
                    order.setStatus(PickupOrderStatus.CANCELLED.getCode());
                    order.setCancelReason("价格确认超时");
                    order.setCancelBy(0L);
                    pickupOrderMapper.updateById(order);

                    // 通知需求者
                    notificationService.send(
                            order.getRequesterId(),
                            NotificationType.PICKUP_TIMEOUT_CANCEL.getCode(),
                            "代拿订单超时取消",
                            "价格确认超时，订单已自动取消",
                            order.getId(), 4,
                            NotificationCategory.PICKUP.getCode()
                    );
                    // 通知代拿者
                    if (order.getPickerId() != null) {
                        notificationService.send(
                                order.getPickerId(),
                                NotificationType.PICKUP_TIMEOUT_CANCEL.getCode(),
                                "代拿订单超时取消",
                                "价格确认超时，订单已自动取消",
                                order.getId(), 4,
                                NotificationCategory.PICKUP.getCode()
                        );
                    }
                    processedCount++;
                } catch (Exception e) {
                    log.error("[代拿价格确认超时任务] 处理失败，订单ID：{}", order.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("[代拿价格确认超时任务] 执行失败：{}", e.getMessage(), e);
        } finally {
            log.info("[代拿价格确认超时任务] 执行完成，处理条数：{}，耗时：{}ms", processedCount, System.currentTimeMillis() - startTime);
        }
    }
}
