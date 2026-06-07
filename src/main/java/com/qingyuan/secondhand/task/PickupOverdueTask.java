package com.qingyuan.secondhand.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingyuan.secondhand.common.enums.NotificationCategory;
import com.qingyuan.secondhand.common.enums.NotificationType;
import com.qingyuan.secondhand.common.enums.PickupOrderStatus;
import com.qingyuan.secondhand.entity.PickupOrder;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.PickupOrderMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "task.enabled.pickup-overdue", havingValue = "true", matchIfMissing = true)
public class PickupOverdueTask {

    private final PickupOrderMapper pickupOrderMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void execute() {
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        log.info("[代拿送达超时任务] 开始执行");
        try {
            // 阶段1：临近超时提醒
            remindNearOverdue();
            // 阶段2：超时自动取消
            processedCount += cancelOverdue();
        } catch (Exception e) {
            log.error("[代拿送达超时任务] 执行失败：{}", e.getMessage(), e);
        } finally {
            log.info("[代拿送达超时任务] 执行完成，处理条数：{}，耗时：{}ms", processedCount, System.currentTimeMillis() - startTime);
        }
    }

    private void remindNearOverdue() {
        try {
            LambdaQueryWrapper<PickupOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.le(PickupOrder::getExpectedDeliveryTime, LocalDateTime.now())
                    .gt(PickupOrder::getDeliveryDeadline, LocalDateTime.now())
                    .eq(PickupOrder::getStatus, PickupOrderStatus.PICKING_UP.getCode())
                    .last("LIMIT 500");
            List<PickupOrder> orders = pickupOrderMapper.selectList(wrapper);
            if (orders == null || orders.isEmpty()) {
                return;
            }
            for (PickupOrder order : orders) {
                try {
                    if (order.getPickerId() != null) {
                        notificationService.send(
                                order.getPickerId(),
                                NotificationType.PICKUP_DELIVERY_REMIND.getCode(),
                                "代拿送达超时提醒",
                                "您的代拿订单即将超时，请尽快送达",
                                order.getId(), 4,
                                NotificationCategory.PICKUP.getCode()
                        );
                    }
                } catch (Exception e) {
                    log.error("[代拿送达超时任务] 提醒失败，订单ID：{}", order.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("[代拿送达超时任务] 临近提醒阶段失败：{}", e.getMessage(), e);
        }
    }

    private int cancelOverdue() {
        int count = 0;
        try {
            LambdaQueryWrapper<PickupOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(PickupOrder::getDeliveryDeadline, LocalDateTime.now())
                    .eq(PickupOrder::getStatus, PickupOrderStatus.PICKING_UP.getCode())
                    .last("LIMIT 500");
            List<PickupOrder> orders = pickupOrderMapper.selectList(wrapper);
            if (orders == null || orders.isEmpty()) {
                return 0;
            }
            for (PickupOrder order : orders) {
                try {
                    order.setStatus(PickupOrderStatus.CANCELLED.getCode());
                    order.setCancelReason("代拿超时");
                    order.setCancelBy(0L);
                    pickupOrderMapper.updateById(order);

                    // 扣减代拿者信用分
                    if (order.getPickerId() != null) {
                        User picker = userMapper.selectById(order.getPickerId());
                        if (picker != null && picker.getPickupScore() != null) {
                            BigDecimal newScore = picker.getPickupScore().subtract(new BigDecimal("0.5"));
                            if (newScore.compareTo(BigDecimal.ZERO) < 0) {
                                newScore = BigDecimal.ZERO;
                            }
                            picker.setPickupScore(newScore);
                            userMapper.updateById(picker);
                        }
                        notificationService.send(
                                order.getPickerId(),
                                NotificationType.PICKUP_TIMEOUT_CANCEL.getCode(),
                                "代拿超时取消",
                                "代拿超时，订单已取消，信用分已扣减",
                                order.getId(), 4,
                                NotificationCategory.PICKUP.getCode()
                        );
                    }
                    // 通知需求者
                    notificationService.send(
                            order.getRequesterId(),
                            NotificationType.PICKUP_TIMEOUT_CANCEL.getCode(),
                            "代拿超时取消",
                            "代拿超时，订单已自动取消，您可以重新发布",
                            order.getId(), 4,
                            NotificationCategory.PICKUP.getCode()
                    );
                    count++;
                } catch (Exception e) {
                    log.error("[代拿送达超时任务] 取消失败，订单ID：{}", order.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("[代拿送达超时任务] 超时取消阶段失败：{}", e.getMessage(), e);
        }
        return count;
    }
}
