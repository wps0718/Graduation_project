package com.qingyuan.secondhand.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingyuan.secondhand.common.enums.NotificationCategory;
import com.qingyuan.secondhand.common.enums.ProductStatus;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.mapper.ProductMapper;
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
@ConditionalOnProperty(name = "task.enabled.product-auto-off", havingValue = "true", matchIfMissing = true)
public class ProductAutoOffTask {

    private final ProductMapper productMapper;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 4 * * ?")
    public void execute() {
        LocalDateTime start = LocalDateTime.now();
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        log.info("[商品自动下架任务] 开始执行，时间：{}", start);
        try {
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(Product::getAutoOffTime, LocalDateTime.now())
                    .eq(Product::getStatus, ProductStatus.ON_SALE.getCode())
                    .eq(Product::getIsDeleted, 0);
            List<Product> products = productMapper.selectList(wrapper);
            if (products == null || products.isEmpty()) {
                log.info("[商品自动下架任务] 无需自动下架的商品");
                return;
            }
            for (Product product : products) {
                try {
                    Product update = new Product();
                    update.setId(product.getId());
                    update.setStatus(ProductStatus.OFF_SHELF.getCode());
                    update.setUpdateTime(LocalDateTime.now());
                    productMapper.updateById(update);

                    notificationService.send(
                            product.getUserId(),
                            3,
                            "商品已下架",
                            "您的商品《" + product.getTitle() + "》已发布超过90天，系统已自动下架",
                            product.getId(),
                            1,
                            NotificationCategory.SYSTEM.getCode()
                    );
                    processedCount++;
                } catch (Exception e) {
                    log.error("[商品自动下架任务] 处理商品失败，商品ID：{}，错误：{}", product.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("[商品自动下架任务] 执行失败：{}", e.getMessage(), e);
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("[商品自动下架任务] 执行完成，处理条数：{}，耗时：{}ms", processedCount, endTime - startTime);
        }
    }
}
