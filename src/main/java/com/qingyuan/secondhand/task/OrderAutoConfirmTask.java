package com.qingyuan.secondhand.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.enums.NotificationCategory;
import com.qingyuan.secondhand.common.enums.OrderStatus;
import com.qingyuan.secondhand.common.enums.ProductStatus;
import com.qingyuan.secondhand.entity.ChatMessage;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.entity.TradeOrder;
import com.qingyuan.secondhand.mapper.ChatMessageMapper;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "task.enabled.order-auto-confirm", havingValue = "true", matchIfMissing = true)
public class OrderAutoConfirmTask {

    private final TradeOrderMapper tradeOrderMapper;
    private final ProductMapper productMapper;
    private final NotificationService notificationService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ChatMessageMapper chatMessageMapper;
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 0 2 * * ?")
    public void execute() {
        LocalDateTime start = LocalDateTime.now();
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        log.info("[订单自动确认任务] 开始执行，时间：{}", start);
        try {
            LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(TradeOrder::getConfirmDeadline, LocalDateTime.now())
                    .in(TradeOrder::getStatus, OrderStatus.PENDING_ACCEPT.getCode(), OrderStatus.PENDING_MEET.getCode())
                    .last("LIMIT 500");
            List<TradeOrder> orders = tradeOrderMapper.selectList(wrapper);
            if (orders == null || orders.isEmpty()) {
                log.info("[订单自动确认任务] 无需自动确认的订单");
                return;
            }

            // 批量查询所有相关订单的聊天消息
            List<Long> orderIds = orders.stream().map(TradeOrder::getId).collect(Collectors.toList());
            List<ChatMessage> orderMessages = chatMessageMapper.selectList(
                    new LambdaQueryWrapper<ChatMessage>()
                            .in(ChatMessage::getOrderId, orderIds)
                            .eq(ChatMessage::getMsgType, 3)
            );
            Map<Long, ChatMessage> messageMap = orderMessages.stream()
                    .collect(Collectors.toMap(ChatMessage::getOrderId, Function.identity(), (a, b) -> a));

            // 批量查询所有涉及的商品
            List<Long> productIds = orders.stream().map(TradeOrder::getProductId).distinct().collect(Collectors.toList());
            List<Product> products = productMapper.selectBatchIds(productIds);
            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, Function.identity()));

            for (TradeOrder order : orders) {
                try {
                    TradeOrder update = new TradeOrder();
                    update.setId(order.getId());
                    update.setStatus(OrderStatus.COMPLETED.getCode());
                    update.setCompleteTime(LocalDateTime.now());
                    update.setUpdateTime(LocalDateTime.now());
                    tradeOrderMapper.updateById(update);

                    // 同步更新聊天消息中的订单状态
                    try {
                        ChatMessage orderMsg = messageMap.get(order.getId());
                        if (orderMsg != null && orderMsg.getContent() != null) {
                            JsonNode root = objectMapper.readTree(orderMsg.getContent());
                            if (root.isObject()) {
                                ((ObjectNode) root).put("status", OrderStatus.COMPLETED.getCode());
                                ChatMessage msgUpdate = new ChatMessage();
                                msgUpdate.setId(orderMsg.getId());
                                msgUpdate.setContent(objectMapper.writeValueAsString(root));
                                chatMessageMapper.updateById(msgUpdate);
                            }
                        }
                    } catch (Exception e) {
                        log.error("[订单自动确认任务] 更新聊天消息状态失败，订单ID：{}", order.getId(), e);
                    }

                    Product product = productMap.get(order.getProductId());
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
