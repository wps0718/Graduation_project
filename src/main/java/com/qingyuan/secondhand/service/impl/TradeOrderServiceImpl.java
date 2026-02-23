package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.enums.NotificationType;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.common.util.OrderNoUtil;
import com.qingyuan.secondhand.common.util.PhoneUtil;
import com.qingyuan.secondhand.dto.OrderCreateDTO;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.entity.TradeOrder;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.ProductMapper;
import com.qingyuan.secondhand.mapper.TradeOrderMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.service.TradeOrderService;
import com.qingyuan.secondhand.vo.AdminOrderPageVO;
import com.qingyuan.secondhand.vo.OrderCreateVO;
import com.qingyuan.secondhand.vo.OrderDetailVO;
import com.qingyuan.secondhand.vo.OrderListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TradeOrderServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrder> implements TradeOrderService {

    private final TradeOrderMapper tradeOrderMapper;
    private final ProductMapper productMapper;
    private final StringRedisTemplate redisTemplate;
    private final NotificationService notificationService;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public OrderCreateVO createOrder(OrderCreateDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (dto == null || dto.getProductId() == null) {
            throw new BusinessException("商品ID不能为空");
        }
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || Integer.valueOf(1).equals(product.getIsDeleted())) {
            throw new BusinessException("商品不存在");
        }
        if (!Integer.valueOf(1).equals(product.getStatus())) {
            throw new BusinessException("商品未在售");
        }
        if (userId.equals(product.getUserId())) {
            throw new BusinessException("不能购买自己的商品");
        }

        String lockKey = RedisConstant.PRODUCT_LOCK_PREFIX + dto.getProductId();
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, 30, TimeUnit.SECONDS);
        if (locked == null || !locked) {
            throw new BusinessException("商品正在被其他用户下单，请稍后重试");
        }

        try {
            Long count = tradeOrderMapper.selectCount(new LambdaQueryWrapper<TradeOrder>()
                    .eq(TradeOrder::getProductId, dto.getProductId())
                    .eq(TradeOrder::getStatus, 1));
            if (count != null && count > 0) {
                throw new BusinessException("该商品已有进行中的订单");
            }
            TradeOrder order = new TradeOrder();
            order.setOrderNo(OrderNoUtil.generate());
            order.setProductId(dto.getProductId());
            order.setBuyerId(userId);
            order.setSellerId(product.getUserId());
            order.setPrice(dto.getPrice());
            order.setCampusId(dto.getCampusId());
            order.setMeetingPoint(dto.getMeetingPoint());
            order.setStatus(1);
            order.setExpireTime(LocalDateTime.now().plusHours(72));
            order.setConfirmDeadline(LocalDateTime.now().plusDays(7));
            order.setIsDeletedBuyer(0);
            order.setIsDeletedSeller(0);
            int inserted = tradeOrderMapper.insert(order);
            if (inserted <= 0) {
                throw new BusinessException("创建订单失败");
            }
            notificationService.send(
                    order.getSellerId(),
                    2,
                    "您有新的订单",
                    "买家对您的商品《" + product.getTitle() + "》下单了",
                    order.getId(),
                    2,
                    1
            );
            OrderCreateVO vo = new OrderCreateVO();
            vo.setOrderId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setExpireTime(order.getExpireTime());
            return vo;
        } finally {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            redisTemplate.execute(
                    new DefaultRedisScript<>(script, Long.class),
                    Collections.singletonList(lockKey),
                    lockValue
            );
        }
    }

    @Override
    public IPage<OrderListVO> getOrderList(String role, Integer status, Integer page, Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (!StringUtils.hasText(role)) {
            throw new BusinessException("角色不能为空");
        }
        if (!"buyer".equals(role) && !"seller".equals(role)) {
            throw new BusinessException("角色参数错误");
        }
        Page<OrderListVO> pageObj = new Page<>(page, pageSize);
        return tradeOrderMapper.getOrderList(pageObj, userId, role, status);
    }

    @Override
    public OrderDetailVO getOrderDetail(Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (id == null) {
            throw new BusinessException("订单ID不能为空");
        }
        OrderDetailVO detail = tradeOrderMapper.getOrderDetail(id);
        if (detail == null) {
            throw new BusinessException("订单不存在");
        }
        boolean isBuyer = userId.equals(detail.getBuyerId());
        boolean isSeller = userId.equals(detail.getSellerId());
        if (!isBuyer && !isSeller) {
            throw new BusinessException("无权限查看该订单");
        }
        detail.setCurrentRole(isBuyer ? "buyer" : "seller");
        detail.setBuyerPhone(PhoneUtil.maskPhone(detail.getBuyerPhone()));
        detail.setSellerPhone(PhoneUtil.maskPhone(detail.getSellerPhone()));
        detail.setProductImages(parseImages(detail.getProductImagesJson()));
        return detail;
    }

    @Override
    @Transactional
    public void confirmOrder(Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (orderId == null) {
            throw new BusinessException("订单ID不能为空");
        }
        TradeOrder order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!userId.equals(order.getBuyerId())) {
            throw new BusinessException("只有买家可以确认收货");
        }
        if (!Integer.valueOf(1).equals(order.getStatus())) {
            throw new BusinessException("订单状态不正确");
        }
        Product product = productMapper.selectById(order.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        order.setStatus(3);
        order.setCompleteTime(LocalDateTime.now());
        if (tradeOrderMapper.updateById(order) <= 0) {
            throw new BusinessException("确认收货失败");
        }
        product.setStatus(3);
        if (productMapper.updateById(product) <= 0) {
            throw new BusinessException("商品状态更新失败");
        }
        String productName = product.getTitle() == null ? "商品" : product.getTitle();
        notificationService.send(
                order.getBuyerId(),
                NotificationType.TRADE_SUCCESS,
                Map.of("productName", productName),
                order.getId(),
                2,
                1
        );
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String cancelReason) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (orderId == null) {
            throw new BusinessException("订单ID不能为空");
        }
        TradeOrder order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        boolean isBuyer = userId.equals(order.getBuyerId());
        boolean isSeller = userId.equals(order.getSellerId());
        if (!isBuyer && !isSeller) {
            throw new BusinessException("无权取消该订单");
        }
        if (!Integer.valueOf(1).equals(order.getStatus())) {
            throw new BusinessException("只有待面交的订单可以取消");
        }
        Product product = productMapper.selectById(order.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        order.setStatus(5);
        order.setCancelBy(userId);
        order.setCancelReason(cancelReason);
        if (tradeOrderMapper.updateById(order) <= 0) {
            throw new BusinessException("取消订单失败");
        }
        product.setStatus(1);
        if (productMapper.updateById(product) <= 0) {
            throw new BusinessException("商品状态更新失败");
        }
        Long targetUserId = isBuyer ? order.getSellerId() : order.getBuyerId();
        String productName = product.getTitle() == null ? "商品" : product.getTitle();
        User otherUser = userMapper.selectById(targetUserId);
        String nickName = otherUser == null || !StringUtils.hasText(otherUser.getNickName()) ? "对方" : otherUser.getNickName();
        Map<String, String> params = Map.of("nickName", nickName, "productName", productName);
        notificationService.send(
                targetUserId,
                NotificationType.ORDER_CANCEL,
                params,
                order.getId(),
                2,
                1
        );
    }

    @Override
    public void deleteOrder(Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        if (orderId == null) {
            throw new BusinessException("订单ID不能为空");
        }
        TradeOrder order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        boolean isBuyer = userId.equals(order.getBuyerId());
        boolean isSeller = userId.equals(order.getSellerId());
        if (!isBuyer && !isSeller) {
            throw new BusinessException("无权删除该订单");
        }
        if (!Integer.valueOf(4).equals(order.getStatus()) && !Integer.valueOf(5).equals(order.getStatus())) {
            throw new BusinessException("只有已评价或已取消的订单可以删除");
        }
        if (isBuyer) {
            order.setIsDeletedBuyer(1);
        } else {
            order.setIsDeletedSeller(1);
        }
        if (tradeOrderMapper.updateById(order) <= 0) {
            throw new BusinessException("删除订单失败");
        }
    }

    @Override
    public IPage<AdminOrderPageVO> getAdminOrderPage(Integer page, Integer pageSize, Integer status) {
        if (page == null || pageSize == null) {
            throw new BusinessException("分页参数不能为空");
        }
        Page<AdminOrderPageVO> pageObj = new Page<>(page, pageSize);
        Page<AdminOrderPageVO> result = tradeOrderMapper.getAdminOrderPage(pageObj, status);
        if (result != null && result.getRecords() != null) {
            for (AdminOrderPageVO item : result.getRecords()) {
                item.setProductCoverImage(parseCoverImage(item.getProductCoverImage()));
                item.setBuyerPhone(PhoneUtil.maskPhone(item.getBuyerPhone()));
                item.setSellerPhone(PhoneUtil.maskPhone(item.getSellerPhone()));
            }
        }
        return result == null ? new Page<>(page, pageSize) : result;
    }

    @Override
    public OrderDetailVO getAdminOrderDetail(Long id) {
        if (id == null) {
            throw new BusinessException("订单ID不能为空");
        }
        OrderDetailVO detail = tradeOrderMapper.getOrderDetail(id);
        if (detail == null) {
            throw new BusinessException("订单不存在");
        }
        detail.setBuyerPhone(PhoneUtil.maskPhone(detail.getBuyerPhone()));
        detail.setSellerPhone(PhoneUtil.maskPhone(detail.getSellerPhone()));
        detail.setProductImages(parseImages(detail.getProductImagesJson()));
        detail.setCurrentRole(null);
        return detail;
    }

    private List<String> parseImages(String imagesJson) {
        if (!StringUtils.hasText(imagesJson)) {
            return null;
        }
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return null;
        }
    }

    private String parseCoverImage(String imagesJson) {
        List<String> images = parseImages(imagesJson);
        return images == null || images.isEmpty() ? null : images.get(0);
    }
}
