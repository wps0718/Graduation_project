package com.qingyuan.secondhand.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingyuan.secondhand.common.enums.NotificationCategory;
import com.qingyuan.secondhand.common.enums.NotificationType;
import com.qingyuan.secondhand.common.enums.PickupDisputeStatus;
import com.qingyuan.secondhand.common.enums.PickupOrderStatus;
import com.qingyuan.secondhand.entity.PickupDispute;
import com.qingyuan.secondhand.entity.PickupOrder;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.PickupDisputeMapper;
import com.qingyuan.secondhand.mapper.PickupOrderMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "task.enabled.dispute-reminder", havingValue = "true", matchIfMissing = true)
public class DisputeReminderTask {

    private final PickupDisputeMapper pickupDisputeMapper;
    private final PickupOrderMapper pickupOrderMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 */6 * * ?")
    public void execute() {
        long startTime = System.currentTimeMillis();
        log.info("[纠纷提醒与自动裁决任务] 开始执行");
        try {
            remind3Day();
            remind6Day();
            autoJudgment();
        } catch (Exception e) {
            log.error("[纠纷提醒与自动裁决任务] 执行失败：{}", e.getMessage(), e);
        } finally {
            log.info("[纠纷提醒与自动裁决任务] 执行完成，耗时：{}ms", System.currentTimeMillis() - startTime);
        }
    }

    private void remind3Day() {
        try {
            List<PickupDispute> disputes = pickupDisputeMapper.selectList(
                    new LambdaQueryWrapper<PickupDispute>()
                            .eq(PickupDispute::getStatus, PickupDisputeStatus.PENDING_RESPONSE.getCode())
                            .eq(PickupDispute::getReminderCount, 0)
                            .gt(PickupDispute::getResponseDeadline, LocalDateTime.now())
                            .last("LIMIT 500")
            );
            for (PickupDispute dispute : disputes) {
                try {
                    long daysLeft = Duration.between(LocalDateTime.now(), dispute.getResponseDeadline()).toDays();
                    if (daysLeft <= 4 && daysLeft > 1) {
                        Long responderId = getResponderId(dispute);
                        if (responderId != null) {
                            notificationService.send(
                                    responderId,
                                    NotificationType.PICKUP_DISPUTE_3DAY.getCode(),
                                    "纠纷3天提醒",
                                    "纠纷回应还剩" + daysLeft + "天，请尽快提供证据",
                                    dispute.getOrderId(), 4,
                                    NotificationCategory.PICKUP.getCode()
                            );
                        }
                        dispute.setReminderCount(1);
                        dispute.setLastReminderTime(LocalDateTime.now());
                        pickupDisputeMapper.updateById(dispute);
                    }
                } catch (Exception e) {
                    log.error("[纠纷3天提醒] 处理失败，纠纷ID：{}", dispute.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("[纠纷3天提醒] 执行失败：{}", e.getMessage(), e);
        }
    }

    private void remind6Day() {
        try {
            List<PickupDispute> disputes = pickupDisputeMapper.selectList(
                    new LambdaQueryWrapper<PickupDispute>()
                            .eq(PickupDispute::getStatus, PickupDisputeStatus.PENDING_RESPONSE.getCode())
                            .eq(PickupDispute::getReminderCount, 1)
                            .gt(PickupDispute::getResponseDeadline, LocalDateTime.now())
                            .last("LIMIT 500")
            );
            for (PickupDispute dispute : disputes) {
                try {
                    long hoursLeft = Duration.between(LocalDateTime.now(), dispute.getResponseDeadline()).toHours();
                    if (hoursLeft <= 24) {
                        Long responderId = getResponderId(dispute);
                        if (responderId != null) {
                            notificationService.send(
                                    responderId,
                                    NotificationType.PICKUP_DISPUTE_6DAY.getCode(),
                                    "纠纷6天提醒",
                                    "纠纷回应还剩不到1天，逾期将自动判定对方胜诉",
                                    dispute.getOrderId(), 4,
                                    NotificationCategory.PICKUP.getCode()
                            );
                        }
                        dispute.setReminderCount(2);
                        dispute.setLastReminderTime(LocalDateTime.now());
                        pickupDisputeMapper.updateById(dispute);
                    }
                } catch (Exception e) {
                    log.error("[纠纷6天提醒] 处理失败，纠纷ID：{}", dispute.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("[纠纷6天提醒] 执行失败：{}", e.getMessage(), e);
        }
    }

    private void autoJudgment() {
        try {
            List<PickupDispute> disputes = pickupDisputeMapper.selectList(
                    new LambdaQueryWrapper<PickupDispute>()
                            .eq(PickupDispute::getStatus, PickupDisputeStatus.PENDING_RESPONSE.getCode())
                            .lt(PickupDispute::getResponseDeadline, LocalDateTime.now())
                            .last("LIMIT 500")
            );
            for (PickupDispute dispute : disputes) {
                try {
                    dispute.setStatus(PickupDisputeStatus.AUTO_WIN.getCode());
                    dispute.setResolveTime(LocalDateTime.now());
                    pickupDisputeMapper.updateById(dispute);

                    // 扣减被申诉方信用分
                    Long responderId = getResponderId(dispute);
                    if (responderId != null) {
                        User responder = userMapper.selectById(responderId);
                        if (responder != null && responder.getPickupScore() != null) {
                            BigDecimal newScore = responder.getPickupScore().subtract(new BigDecimal("1.0"));
                            if (newScore.compareTo(BigDecimal.ZERO) < 0) {
                                newScore = BigDecimal.ZERO;
                            }
                            responder.setPickupScore(newScore);
                            userMapper.updateById(responder);
                        }
                    }

                    // 更新订单状态为已取消
                    PickupOrder order = pickupOrderMapper.selectById(dispute.getOrderId());
                    if (order != null) {
                        order.setStatus(PickupOrderStatus.CANCELLED.getCode());
                        order.setCancelReason("纠纷超时未回应，申诉方自动胜诉");
                        order.setCancelBy(0L);
                        pickupOrderMapper.updateById(order);
                    }

                    // 通知双方
                    notificationService.send(
                            dispute.getInitiatorId(),
                            NotificationType.PICKUP_DISPUTE_AUTO_WIN.getCode(),
                            "纠纷超时自动裁决",
                            "对方未回应，申诉方自动胜诉",
                            dispute.getOrderId(), 4,
                            NotificationCategory.PICKUP.getCode()
                    );
                    if (responderId != null) {
                        notificationService.send(
                                responderId,
                                NotificationType.PICKUP_DISPUTE_AUTO_WIN.getCode(),
                                "纠纷超时自动裁决",
                                "您未在规定时间内回应，申诉方自动胜诉",
                                dispute.getOrderId(), 4,
                                NotificationCategory.PICKUP.getCode()
                        );
                    }
                } catch (Exception e) {
                    log.error("[纠纷自动裁决] 处理失败，纠纷ID：{}", dispute.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("[纠纷自动裁决] 执行失败：{}", e.getMessage(), e);
        }
    }

    private Long getResponderId(PickupDispute dispute) {
        PickupOrder order = pickupOrderMapper.selectById(dispute.getOrderId());
        if (order == null) return null;
        // 发起人是需求者 → 被申诉方是代拿者，反之亦然
        if (dispute.getInitiatorId().equals(order.getRequesterId())) {
            return order.getPickerId();
        } else {
            return order.getRequesterId();
        }
    }
}
