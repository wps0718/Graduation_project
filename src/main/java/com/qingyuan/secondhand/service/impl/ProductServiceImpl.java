package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.qingyuan.secondhand.entity.CampusAuth;
import com.qingyuan.secondhand.entity.College;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.entity.TradeOrder;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.CampusAuthMapper;
import com.qingyuan.secondhand.mapper.CollegeMapper;
import com.qingyuan.secondhand.mapper.ProductMapper;
import com.qingyuan.secondhand.mapper.TradeOrderMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.service.ProductService;
import com.qingyuan.secondhand.vo.AdminProductPageVO;
import com.qingyuan.secondhand.vo.ProductDetailVO;
import com.qingyuan.secondhand.vo.ProductListVO;
import com.qingyuan.secondhand.vo.PublisherInfoVO;
import com.qingyuan.secondhand.vo.RelatedOrderVO;
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
import java.util.Set;
import java.util.stream.Collectors;
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
    private final TradeOrderMapper tradeOrderMapper;
    private final UserMapper userMapper;
    private final CampusAuthMapper campusAuthMapper;
    private final CollegeMapper collegeMapper;

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
    @Transactional(rollbackFor = Exception.class)
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

        // 发送站内通知：商品已提交重新审核
        notificationService.send(
                existing.getUserId(),
                3,
                "商品已提交重新审核",
                "您的商品《" + existing.getTitle() + "》已提交修改，等待审核",
                dto.getProductId(),
                1,
                2
        );

        stringRedisTemplate.delete(RedisConstant.USER_STATS + existing.getUserId());
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
    public IPage<ProductListVO> getMyProductList(Integer page, Integer pageSize, Integer status,
                                                  String keyword, String sortBy, String order) {
        Long userId = UserContext.getCurrentUserId();
        log.info("📍 [ProductService] 获取我的商品列表，用户ID: {}, 页码: {}, 状态: {}, 关键词: {}, 排序: {}_{}",
                userId, page, status, keyword, sortBy, order);

        if (userId == null) {
            log.error("❌ [ProductService] 用户ID为空！");
            throw new BusinessException("未登录");
        }

        Page<ProductListVO> pageObj = new Page<>(page, pageSize);
        Page<ProductListVO> result = productMapper.getMyProductList(pageObj, userId, status, keyword, sortBy, order);
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

        stringRedisTemplate.delete(RedisConstant.USER_STATS + product.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markSold(Long productId) {
        Long userId = UserContext.getCurrentUserId();
        log.info("📍 [ProductService] 标记商品售出，商品ID: {}, 用户ID: {}", productId, userId);

        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        if (!product.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此商品");
        }

        if (product.getStatus() != 1) {
            throw new BusinessException("只有在售商品才能标记售出");
        }

        product.setStatus(3);
        this.updateById(product);

        stringRedisTemplate.delete("product:detail:" + productId);
        stringRedisTemplate.delete(RedisConstant.USER_STATS + product.getUserId());

        log.info("✅ [ProductService] 商品已标记为售出，商品ID: {}", productId);
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

        stringRedisTemplate.delete(RedisConstant.USER_STATS + product.getUserId());
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
        update.setReviewTime(now);
        update.setReviewerId(reviewerId);
        update.setRejectReason(null);

        // 检查该商品是否有进行中的订单（防止状态不一致）
        LambdaQueryWrapper<TradeOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(TradeOrder::getProductId, productId)
                .in(TradeOrder::getStatus, 1, 2);
        long activeOrders = tradeOrderMapper.selectCount(orderWrapper);
        if (activeOrders > 0) {
            log.warn("商品[{}]审核通过时存在进行中订单({}条)，状态设为已售出", productId, activeOrders);
            update.setStatus(3);
        } else {
            update.setStatus(1);
        }

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

        stringRedisTemplate.delete(RedisConstant.USER_STATS + product.getUserId());
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

        // 先查询所有受影响的 userId，用于后续清除缓存
        List<Product> productList = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .in(Product::getId, productIds)
                .eq(Product::getStatus, 0)
                .eq(Product::getIsDeleted, 0));
        if (productList.isEmpty()) {
            throw new BusinessException("批量审核失败");
        }

        LocalDateTime now = LocalDateTime.now();
        Long reviewerId = UserContext.getCurrentUserId();

        // 检查哪些商品有进行中的订单
        LambdaQueryWrapper<TradeOrder> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.in(TradeOrder::getProductId, productIds)
                .in(TradeOrder::getStatus, 1, 2);
        List<TradeOrder> activeOrders = tradeOrderMapper.selectList(orderQuery);
        Set<Long> productIdsWithActiveOrders = activeOrders.stream()
                .map(TradeOrder::getProductId)
                .collect(Collectors.toSet());

        if (!productIdsWithActiveOrders.isEmpty()) {
            // 有进行中订单的商品 → 设为已售出(3)
            LambdaUpdateWrapper<Product> soldWrapper = new LambdaUpdateWrapper<>();
            soldWrapper.in(Product::getId, productIdsWithActiveOrders)
                    .eq(Product::getStatus, 0)
                    .eq(Product::getIsDeleted, 0)
                    .set(Product::getStatus, 3)
                    .set(Product::getReviewTime, now)
                    .set(Product::getReviewerId, reviewerId)
                    .set(Product::getRejectReason, null);
            productMapper.update(null, soldWrapper);
            for (Long pid : productIdsWithActiveOrders) {
                log.warn("商品[{}]批量审核通过时存在进行中订单，状态设为已售出", pid);
            }
        }

        // 无进行中订单的商品 → 设为在售(1)
        List<Long> normalIds = productList.stream()
                .map(Product::getId)
                .filter(id -> !productIdsWithActiveOrders.contains(id))
                .toList();
        if (!normalIds.isEmpty()) {
            LambdaUpdateWrapper<Product> normalWrapper = new LambdaUpdateWrapper<>();
            normalWrapper.in(Product::getId, normalIds)
                    .eq(Product::getStatus, 0)
                    .eq(Product::getIsDeleted, 0)
                    .set(Product::getStatus, 1)
                    .set(Product::getReviewTime, now)
                    .set(Product::getReviewerId, reviewerId)
                    .set(Product::getRejectReason, null);
            productMapper.update(null, normalWrapper);
        }

        // 清除所有受影响用户的 stats 缓存
        productList.stream()
                .map(Product::getUserId)
                .distinct()
                .forEach(uid -> stringRedisTemplate.delete(RedisConstant.USER_STATS + uid));
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

        // 检查是否有进行中的订单（防止强制下架后订单无法履约）
        LambdaQueryWrapper<TradeOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(TradeOrder::getProductId, productId)
                .in(TradeOrder::getStatus, 1, 2);
        long activeOrders = tradeOrderMapper.selectCount(orderWrapper);
        if (activeOrders > 0) {
            throw new BusinessException("该商品存在进行中的订单，无法下架");
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

        stringRedisTemplate.delete(RedisConstant.USER_STATS + product.getUserId());
    }

    @Override
    public IPage<RelatedOrderVO> getRelatedOrders(Long productId, Integer page, Integer pageSize) {
        if (productId == null) {
            throw new BusinessException("商品ID不能为空");
        }
        Page<RelatedOrderVO> pageObj = new Page<>(page, pageSize);
        Page<RelatedOrderVO> result = tradeOrderMapper.getRelatedOrdersByProductId(pageObj, productId);
        if (result != null && result.getRecords() != null) {
            for (RelatedOrderVO vo : result.getRecords()) {
                vo.setStatusText(getOrderStatusText(vo.getStatus()));
                if (Integer.valueOf(5).equals(vo.getStatus())) {
                    vo.setCancelByText(getCancelByText(vo.getCancelBy()));
                }
            }
        }
        return result == null ? new Page<>(page, pageSize) : result;
    }

    @Override
    public PublisherInfoVO getPublisherInfo(Long productId) {
        if (productId == null) {
            throw new BusinessException("商品ID不能为空");
        }
        Product product = productMapper.selectById(productId);
        if (product == null || Integer.valueOf(1).equals(product.getIsDeleted())) {
            throw new BusinessException("商品不存在");
        }

        Long userId = product.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        PublisherInfoVO vo = new PublisherInfoVO();
        vo.setUserId(user.getId());
        vo.setAvatarUrl(user.getAvatarUrl());
        vo.setNickName(user.getNickName());
        vo.setPhone(maskPhone(user.getPhone()));
        vo.setAccountStatus(user.getStatus());
        vo.setAccountStatusText(Integer.valueOf(1).equals(user.getStatus()) ? "正常" : "禁用");
        vo.setAuthStatus(user.getAuthStatus());
        vo.setAuthStatusText(getAuthStatusText(user.getAuthStatus()));
        vo.setScore(user.getScore());
        vo.setBio(user.getBio());
        vo.setIpRegion(user.getIpRegion());
        vo.setCreateTime(user.getCreateTime());

        vo.setProductCount(productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getUserId, userId)
                .eq(Product::getIsDeleted, 0)).intValue());

        vo.setDealOrderCount(tradeOrderMapper.selectCount(new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getSellerId, userId)
                .in(TradeOrder::getStatus, 3, 4)).intValue());

        CampusAuth auth = campusAuthMapper.selectByUserId(userId);
        if (auth != null) {
            vo.setRealName(maskRealName(auth.getRealName()));
            vo.setStudentNo(maskStudentNo(auth.getStudentNo()));
            College college = collegeMapper.selectById(auth.getCollegeId());
            if (college != null) {
                vo.setCollegeName(college.getName());
            }
        }

        return vo;
    }

    private String getOrderStatusText(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 1: return "待面交";
            case 2: return "预留";
            case 3: return "已完成";
            case 4: return "已评价";
            case 5: return "已取消";
            default: return "未知";
        }
    }

    private String getCancelByText(Integer cancelBy) {
        if (cancelBy == null) return "系统取消";
        switch (cancelBy) {
            case 0: return "系统取消";
            case 1: return "买家取消";
            case 2: return "卖家取消";
            default: return "未知";
        }
    }

    private String getAuthStatusText(Integer authStatus) {
        if (authStatus == null) return "未认证";
        switch (authStatus) {
            case 0: return "未认证";
            case 1: return "审核中";
            case 2: return "已认证";
            case 3: return "已驳回";
            default: return "未知";
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String maskRealName(String realName) {
        if (realName == null || realName.isEmpty()) return null;
        int len = realName.length();
        if (len == 1) return realName + "*";
        if (len == 2) return realName.charAt(0) + "*";
        // 复姓+单名（如"欧阳明" → "欧**"），或复姓+双名（如"欧阳小明"→ "欧***"）
        return realName.charAt(0) + "*".repeat(len - 1);
    }

    private String maskStudentNo(String studentNo) {
        if (studentNo == null || studentNo.length() < 6) return studentNo;
        return studentNo.substring(0, 4) + "****" + studentNo.substring(studentNo.length() - 2);
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
