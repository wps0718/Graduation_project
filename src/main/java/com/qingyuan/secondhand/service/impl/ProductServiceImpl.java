package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.enums.NotificationType;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.ProductPublishDTO;
import com.qingyuan.secondhand.dto.ProductUpdateDTO;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.mapper.ProductMapper;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.service.ProductService;
import com.qingyuan.secondhand.vo.AdminProductPageVO;
import com.qingyuan.secondhand.vo.ProductDetailVO;
import com.qingyuan.secondhand.vo.ProductListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductMapper productMapper;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ProductAsyncService productAsyncService;
    private final NotificationService notificationService;

    @Override
    public void publishProduct(ProductPublishDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        Product product = new Product();
        product.setUserId(userId);
        fillProduct(product, dto);
        product.setViewCount(0);
        product.setFavoriteCount(0);
        product.setStatus(0);
        product.setIsDeleted(0);
        product.setAutoOffTime(LocalDateTime.now().plusDays(90));
        int inserted = productMapper.insert(product);
        if (inserted <= 0) {
            throw new BusinessException("发布商品失败");
        }
    }

    @Override
    public void updateProduct(ProductUpdateDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        Product existing = productMapper.selectById(dto.getProductId());
        if (existing == null) {
            throw new BusinessException("商品不存在");
        }
        if (!userId.equals(existing.getUserId())) {
            throw new BusinessException("无权编辑该商品");
        }

        Product product = new Product();
        product.setId(dto.getProductId());
        fillProduct(product, dto);
        product.setStatus(0);
        product.setRejectReason(null);
        product.setAutoOffTime(LocalDateTime.now().plusDays(90));
        int updated = productMapper.updateById(product);
        if (updated <= 0) {
            throw new BusinessException("更新商品失败");
        }
    }

    @Override
    public void updatePrice(Long productId, BigDecimal price) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("价格必须大于0");
        }
        Product existing = productMapper.selectById(productId);
        if (existing == null) {
            throw new BusinessException("商品不存在");
        }
        if (!userId.equals(existing.getUserId())) {
            throw new BusinessException("无权修改该商品");
        }
        Product product = new Product();
        product.setId(productId);
        product.setPrice(price);
        int updated = productMapper.updateById(product);
        if (updated <= 0) {
            throw new BusinessException("修改价格失败");
        }
    }

    @Override
    public ProductDetailVO getProductDetail(Long productId) {
        ProductDetailVO detail = productMapper.getProductDetailById(productId);
        if (detail == null) {
            throw new BusinessException("商品不存在");
        }
        detail.setImages(parseImages(detail.getImagesJson()));
        Long userId = UserContext.getCurrentUserId();
        if (userId != null) {
            detail.setIsOwner(userId.equals(detail.getSellerId()));
            Integer favoriteCount = productMapper.countFavoriteByUserAndProduct(userId, productId);
            detail.setIsFavorited(favoriteCount != null && favoriteCount > 0);
            Integer activeCount = productMapper.countActiveOrderByUserAndProduct(userId, productId);
            detail.setHasActiveOrder(activeCount != null && activeCount > 0);
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            String viewKey = RedisConstant.PRODUCT_VIEW + productId + ":" + userId;
            Boolean firstView = ops.setIfAbsent(viewKey, "1", 24, TimeUnit.HOURS);
            if (Boolean.TRUE.equals(firstView)) {
                productAsyncService.asyncUpdateViewCount(productId);
            }
        } else {
            detail.setIsOwner(false);
            detail.setIsFavorited(false);
            detail.setHasActiveOrder(false);
            productAsyncService.asyncUpdateViewCount(productId);
        }
        return detail;
    }

    @Override
    public IPage<ProductListVO> getProductList(Integer page, Integer pageSize, Long campusId, Long categoryId, String keyword,
                                               BigDecimal minPrice, BigDecimal maxPrice, String sortBy) {
        Page<ProductListVO> pageObj = new Page<>(page, pageSize);
        Page<ProductListVO> result = productMapper.getProductList(pageObj, campusId, categoryId, keyword, minPrice, maxPrice, sortBy);
        if (result != null && result.getRecords() != null) {
            for (ProductListVO item : result.getRecords()) {
                item.setCoverImage(parseCoverImage(item.getCoverImage()));
            }
        }
        if (StringUtils.hasText(keyword)) {
            productAsyncService.asyncRecordSearchKeyword(keyword);
        }
        return result == null ? new Page<>(page, pageSize) : result;
    }

    @Override
    public IPage<ProductListVO> getMyProductList(Integer page, Integer pageSize, Integer status) {
        Long userId = UserContext.getCurrentUserId();
        log.info("📍 [ProductService] 获取我的商品列表，用户ID: {}, 页码: {}, 状态: {}", userId, page, status);

        if (userId == null) {
            log.error("❌ [ProductService] 用户ID为空！");
            throw new BusinessException("未登录");
        }

        Page<ProductListVO> pageObj = new Page<>(page, pageSize);
        Page<ProductListVO> result = productMapper.getMyProductList(pageObj, userId, status);
        if (result != null && result.getRecords() != null) {
            for (ProductListVO item : result.getRecords()) {
                item.setCoverImage(parseCoverImage(item.getCoverImage()));
            }
        }

        log.info("✅ [ProductService] 返回 {} 条商品记录",
                result == null || result.getRecords() == null ? 0 : result.getRecords().size());
        return result == null ? new Page<>(page, pageSize) : result;
    }

    @Override
    public void offShelf(Long productId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (productId == null) {
            throw new BusinessException("商品ID不能为空");
        }
        Product product = productMapper.selectById(productId);
        if (product == null || Integer.valueOf(1).equals(product.getIsDeleted())) {
            throw new BusinessException("商品不存在");
        }
        if (!userId.equals(product.getUserId())) {
            throw new BusinessException("无权操作该商品");
        }
        if (!Integer.valueOf(1).equals(product.getStatus())) {
            throw new BusinessException("商品状态不允许下架");
        }
        Integer activeOrders = productMapper.countActiveOrderByProduct(productId);
        if (activeOrders != null && activeOrders > 0) {
            throw new BusinessException("有进行中的订单，无法下架");
        }
        Product update = new Product();
        update.setId(productId);
        update.setStatus(2);
        int updated = productMapper.updateById(update);
        if (updated <= 0) {
            throw new BusinessException("下架失败");
        }
    }

    @Override
    public void onShelf(Long productId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (productId == null) {
            throw new BusinessException("商品ID不能为空");
        }
        Product product = productMapper.selectById(productId);
        if (product == null || Integer.valueOf(1).equals(product.getIsDeleted())) {
            throw new BusinessException("商品不存在");
        }
        if (!userId.equals(product.getUserId())) {
            throw new BusinessException("无权操作该商品");
        }
        if (!Integer.valueOf(2).equals(product.getStatus())) {
            throw new BusinessException("商品状态不允许上架");
        }
        Product update = new Product();
        update.setId(productId);
        update.setStatus(0);
        update.setRejectReason(null);
        int updated = productMapper.updateById(update);
        if (updated <= 0) {
            throw new BusinessException("上架失败");
        }
    }

    @Override
    public void deleteProduct(Long productId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (productId == null) {
            throw new BusinessException("商品ID不能为空");
        }
        Product product = productMapper.selectById(productId);
        if (product == null || Integer.valueOf(1).equals(product.getIsDeleted())) {
            throw new BusinessException("商品不存在");
        }
        if (!userId.equals(product.getUserId())) {
            throw new BusinessException("无权操作该商品");
        }
        Integer activeOrders = productMapper.countActiveOrderByProduct(productId);
        if (activeOrders != null && activeOrders > 0) {
            throw new BusinessException("有进行中的订单，无法删除");
        }
        Product update = new Product();
        update.setId(productId);
        update.setIsDeleted(1);
        int updated = productMapper.updateById(update);
        if (updated <= 0) {
            throw new BusinessException("删除失败");
        }
    }

    @Override
    public IPage<AdminProductPageVO> getAdminProductPage(Integer page,
                                                         Integer pageSize,
                                                         Integer status,
                                                         Long categoryId,
                                                         String keyword,
                                                         BigDecimal minPrice,
                                                         BigDecimal maxPrice,
                                                         String beginTime,
                                                         String endTime,
                                                         String sortBy) {
        Page<AdminProductPageVO> pageObj = new Page<>(page, pageSize);
        LocalDateTime begin = parseDateStart(beginTime);
        LocalDateTime end = parseDateEnd(endTime);
        Page<AdminProductPageVO> result = productMapper.getAdminProductPage(pageObj, status, categoryId, keyword, minPrice, maxPrice, begin, end, sortBy);
        if (result != null && result.getRecords() != null) {
            for (AdminProductPageVO item : result.getRecords()) {
                item.setCoverImage(parseCoverImage(item.getCoverImage()));
            }
        }
        return result == null ? new Page<>(page, pageSize) : result;
    }

    @Override
    public List<AdminProductPageVO> exportAdminProductList(Integer status,
                                                           Long categoryId,
                                                           String keyword,
                                                           BigDecimal minPrice,
                                                           BigDecimal maxPrice,
                                                           String beginTime,
                                                           String endTime,
                                                           String sortBy) {
        LocalDateTime begin = parseDateStart(beginTime);
        LocalDateTime end = parseDateEnd(endTime);
        List<AdminProductPageVO> rows = productMapper.exportAdminProductList(status, categoryId, keyword, minPrice, maxPrice, begin, end, sortBy);
        if (rows != null) {
            for (AdminProductPageVO item : rows) {
                item.setCoverImage(parseCoverImage(item.getCoverImage()));
            }
        }
        return rows;
    }

    private LocalDateTime parseDateStart(String date) {
        if (!StringUtils.hasText(date)) {
            return null;
        }
        try {
            return LocalDate.parse(date).atStartOfDay();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalDateTime parseDateEnd(String date) {
        if (!StringUtils.hasText(date)) {
            return null;
        }
        try {
            // 结束时间取当天最后一刻
            return LocalDate.parse(date).atTime(23, 59, 59);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Override
    public ProductDetailVO getAdminProductDetail(Long productId) {
        if (productId == null) {
            throw new BusinessException("商品ID不能为空");
        }
        ProductDetailVO detail = productMapper.getProductDetailById(productId);
        if (detail == null) {
            throw new BusinessException("商品不存在");
        }
        detail.setImages(parseImages(detail.getImagesJson()));
        detail.setIsOwner(false);
        detail.setIsFavorited(false);
        detail.setHasActiveOrder(false);
        return detail;
    }

    @Override
    public void approveProduct(Long productId) {
        if (productId == null) {
            throw new BusinessException("商品ID不能为空");
        }
        Product product = productMapper.selectById(productId);
        if (product == null || Integer.valueOf(1).equals(product.getIsDeleted())) {
            throw new BusinessException("商品不存在");
        }
        if (!Integer.valueOf(0).equals(product.getStatus())) {
            throw new BusinessException("商品状态不允许审核");
        }
        LocalDateTime now = LocalDateTime.now();
        Long reviewerId = UserContext.getCurrentUserId();
        Product update = new Product();
        update.setId(productId);
        update.setStatus(1);
        update.setReviewTime(now);
        update.setReviewerId(reviewerId);
        update.setRejectReason(null);
        int updated = productMapper.updateById(update);
        if (updated <= 0) {
            throw new BusinessException("审核失败");
        }
        String productName = product.getTitle() == null ? "" : product.getTitle();
        notificationService.send(
                product.getUserId(),
                NotificationType.AUDIT_PASS,
                Map.of("productName", productName),
                product.getId(),
                1,
                2
        );
    }

    @Override
    public void rejectProduct(Long productId, String rejectReason) {
        if (productId == null) {
            throw new BusinessException("商品ID不能为空");
        }
        if (!StringUtils.hasText(rejectReason)) {
            throw new BusinessException("驳回原因不能为空");
        }
        Product product = productMapper.selectById(productId);
        if (product == null || Integer.valueOf(1).equals(product.getIsDeleted())) {
            throw new BusinessException("商品不存在");
        }
        if (!Integer.valueOf(0).equals(product.getStatus())) {
            throw new BusinessException("商品状态不允许审核");
        }
        LocalDateTime now = LocalDateTime.now();
        Long reviewerId = UserContext.getCurrentUserId();
        Product update = new Product();
        update.setId(productId);
        update.setStatus(4);
        update.setRejectReason(rejectReason);
        update.setReviewTime(now);
        update.setReviewerId(reviewerId);
        int updated = productMapper.updateById(update);
        if (updated <= 0) {
            throw new BusinessException("审核失败");
        }
        String productName = product.getTitle() == null ? "" : product.getTitle();
        notificationService.send(
                product.getUserId(),
                NotificationType.AUDIT_REJECT,
                Map.of("productName", productName, "reason", rejectReason),
                product.getId(),
                1,
                2
        );
    }

    @Override
    @Transactional
    public void batchApproveProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BusinessException("商品ID不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        Long reviewerId = UserContext.getCurrentUserId();
        LambdaUpdateWrapper<Product> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(Product::getId, productIds)
                .eq(Product::getStatus, 0)
                .eq(Product::getIsDeleted, 0)
                .set(Product::getStatus, 1)
                .set(Product::getReviewTime, now)
                .set(Product::getReviewerId, reviewerId)
                .set(Product::getRejectReason, null);
        int updated = productMapper.update(null, wrapper);
        if (updated <= 0) {
            throw new BusinessException("批量审核失败");
        }
    }

    @Override
    public void forceOffShelf(Long productId) {
        if (productId == null) {
            throw new BusinessException("商品ID不能为空");
        }
        Product product = productMapper.selectById(productId);
        if (product == null || Integer.valueOf(1).equals(product.getIsDeleted())) {
            throw new BusinessException("商品不存在");
        }
        if (!Integer.valueOf(1).equals(product.getStatus())) {
            throw new BusinessException("商品状态不允许下架");
        }
        Product update = new Product();
        update.setId(productId);
        update.setStatus(2);
        int updated = productMapper.updateById(update);
        if (updated <= 0) {
            throw new BusinessException("强制下架失败");
        }
        notificationService.send(
                product.getUserId(),
                2,
                "商品被强制下架",
                "您的商品《" + product.getTitle() + "》因违规被强制下架",
                product.getId(),
                1,
                2
        );
    }

    private void fillProduct(Product product, ProductPublishDTO dto) {
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setCategoryId(dto.getCategoryId());
        product.setConditionLevel(dto.getConditionLevel());
        product.setCampusId(dto.getCampusId());
        product.setMeetingPointId(dto.getMeetingPointId());
        product.setMeetingPointText(dto.getMeetingPointText());
        product.setImages(toJson(dto.getImages()));
    }

    private String toJson(List<String> images) {
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            throw new BusinessException("图片处理失败");
        }
    }

    private List<String> parseImages(String imagesJson) {
        if (!StringUtils.hasText(imagesJson)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private String parseCoverImage(String imagesJson) {
        List<String> images = parseImages(imagesJson);
        return images.isEmpty() ? null : images.get(0);
    }
}
