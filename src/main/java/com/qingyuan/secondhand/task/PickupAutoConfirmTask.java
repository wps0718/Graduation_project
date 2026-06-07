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
@ConditionalOnProperty(name = "task.enabled.pickup-auto-confirm", havingValue = "true", matchIfMissing = true)
public class PickupAutoConfirmTask {

    private final PickupOrderMapper pickupOrderMapper;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void execute() {
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        log.info("[代拿自动确认完成任务] 开始执行");
        try {
            LambdaQueryWrapper<PickupOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(PickupOrder::getConfirmDeadline, LocalDateTime.now())
                    .eq(PickupOrder::getStatus, PickupOrderStatus.DELIVERED.getCode())
                    .last("LIMIT 500");
            List<PickupOrder> orders = pickupOrderMapper.selectList(wrapper);
            if (orders == null || orders.isEmpty()) {
                log.info("[代拿自动确认完成任务] 无待自动确认订单");
                return;
            }

            for (PickupOrder order : orders) {
                try {
                    order.setRequesterConfirmed(1);
                    order.setStatus(PickupOrderStatus.COMPLETED.getCode());
                    order.setCompleteTime(LocalDateTime.now());
                    pickupOrderMapper.updateById(order);

                    notificationService.send(
                            order.getRequesterId(),
                            NotificationType.PICKUP_AUTO_CONFIRMED.getCode(),
                            "代拿订单自动确认完成",
                            "订单已自动确认完成",
                            order.getId(), 4,
                            NotificationCategory.PICKUP.getCode()
                    );
                    if (order.getPickerId() != null) {
                        notificationService.send(
                                order.getPickerId(),
                                NotificationType.PICKUP_AUTO_CONFIRMED.getCode(),
                                "代拿订单自动确认完成",
                                "订单已自动确认完成",
                                order.getId(), 4,
                                NotificationCategory.PICKUP.getCode()
                        );
                    }
                    processedCount++;
                } catch (Exception e) {
                    log.error("[代拿自动确认完成任务] 处理失败，订单ID：{}", order.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("[代拿自动确认完成任务] 执行失败：{}", e.getMessage(), e);
        } finally {
            log.info("[代拿自动确认完成任务] 执行完成，处理条数：{}，耗时：{}ms", processedCount, System.currentTimeMillis() - startTime);
        }
    }
}
