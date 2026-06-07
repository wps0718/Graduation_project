package com.qingyuan.secondhand.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.enums.NotificationCategory;
import com.qingyuan.secondhand.common.enums.NotificationType;
import com.qingyuan.secondhand.common.enums.PickupDisputeStatus;
import com.qingyuan.secondhand.common.enums.PickupOrderStatus;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.common.util.AESUtil;
import com.qingyuan.secondhand.dto.*;
import com.qingyuan.secondhand.entity.PickupDispute;
import com.qingyuan.secondhand.entity.PickupDisputeEvidence;
import com.qingyuan.secondhand.entity.PickupOrder;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.CampusMapper;
import com.qingyuan.secondhand.mapper.PickupDisputeEvidenceMapper;
import com.qingyuan.secondhand.mapper.PickupDisputeMapper;
import com.qingyuan.secondhand.mapper.PickupOrderMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.service.PickupService;
import com.qingyuan.secondhand.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PickupServiceImpl extends ServiceImpl<PickupOrderMapper, PickupOrder> implements PickupService {

    private final PickupOrderMapper pickupOrderMapper;
    private final PickupDisputeMapper pickupDisputeMapper;
    private final PickupDisputeEvidenceMapper pickupDisputeEvidenceMapper;
    private final UserMapper userMapper;
    private final CampusMapper campusMapper;
    private final NotificationService notificationService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${pickup.aes-key:default-pickup-aes-key-2026}")
    private String aesKey;

    // ========== 订单操作 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PickupOrderDetailVO createOrder(PickupCreateDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("账号状态异常");
        }

        if (dto.getExpectedDeliveryTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("期望送达时间不能早于当前时间");
        }

        PickupOrder order = new PickupOrder();
        order.setOrderNo(generateOrderNo());
        order.setRequesterId(userId);
        order.setCampusId(user.getCampusId());
        order.setPickupCode(AESUtil.encrypt(dto.getPickupCode(), aesKey));
        order.setPickupLocation(dto.getPickupLocation());
        order.setPickupDetail(dto.getPickupDetail());
        order.setDeliveryLocation(dto.getDeliveryLocation());
        order.setExpectedDeliveryTime(dto.getExpectedDeliveryTime());
        order.setProposedPrice(dto.getProposedPrice());
        order.setPickerPriceConfirmed(0);
        order.setPriceDeadline(LocalDateTime.now().plusHours(24));
        order.setRequesterConfirmed(0);
        order.setPickerConfirmed(0);
        order.setStatus(PickupOrderStatus.PENDING_ACCEPT.getCode());
        order.setIsDeleted(0);

        pickupOrderMapper.insert(order);
        log.info("代拿订单创建成功，订单号：{}", order.getOrderNo());

        return getOrderDetail(order.getId());
    }

    @Override
    public IPage<PickupOrderListVO> getOrderList(String role, Integer status, Integer page, Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Page<PickupOrderListVO> pageParam = new Page<>(page, pageSize);
        return pickupOrderMapper.getPickupOrderList(pageParam, userId, role, status != null ? List.of(status) : null);
    }

    @Override
    public PickupOrderDetailVO getOrderDetail(Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        PickupOrderDetailVO detail = pickupOrderMapper.getOrderDetail(id);
        if (detail == null) {
            throw new BusinessException("订单不存在");
        }

        // 兜底修复evidenceImages格式
        if (detail.getEvidenceImages() != null && !detail.getEvidenceImages().isEmpty()) {
            String fixed = normalizeEvidenceJson(detail.getEvidenceImages(), id);
            if (!fixed.equals(detail.getEvidenceImages())) {
                detail.setEvidenceImages(fixed);
            }
        }

        // 取件码仅需求者和已接单代拿者可见
        if (detail.getPickupCode() != null) {
            boolean isRequester = userId.equals(detail.getRequesterId());
            boolean isPicker = userId.equals(detail.getPickerId())
                    && detail.getStatus() >= PickupOrderStatus.ACCEPTED.getCode();
            if (isRequester || isPicker) {
                detail.setPickupCode(AESUtil.decrypt(detail.getPickupCode(), aesKey));
            } else {
                detail.setPickupCode(null);
            }
        }

        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptOrder(Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        User user = userMapper.selectById(userId);
        if (!Integer.valueOf(2).equals(user.getAuthStatus())) {
            throw new BusinessException("请先完成校园认证");
        }
        if (user.getPickupScore() != null && user.getPickupScore().compareTo(new BigDecimal("3.0")) < 0) {
            throw new BusinessException("信用分过低，暂时无法接单");
        }

        String lockKey = RedisConstant.PICKUP_ORDER_LOCK + orderId;
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, 30, TimeUnit.SECONDS);
        if (locked == null || !locked) {
            throw new BusinessException("该订单正在被其他人接单，请稍后重试");
        }

        try {
            PickupOrder order = pickupOrderMapper.selectById(orderId);
            if (order == null) {
                throw new BusinessException("订单不存在");
            }
            if (!Integer.valueOf(PickupOrderStatus.PENDING_ACCEPT.getCode()).equals(order.getStatus())) {
                throw new BusinessException("订单已被接单或已取消");
            }
            if (userId.equals(order.getRequesterId())) {
                throw new BusinessException("不能接自己的订单");
            }

            order.setPickerId(userId);
            order.setStatus(PickupOrderStatus.ACCEPTED.getCode());
            pickupOrderMapper.updateById(order);

            // 通知需求者
            sendNotification(order.getRequesterId(), NotificationType.PICKUP_ACCEPTED,
                    Map.of("orderNo", order.getOrderNo()), order.getId());
        } finally {
            unlock(lockKey, lockValue);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyPrice(PickupPriceModifyDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        String lockKey = RedisConstant.PICKUP_PRICE_LOCK + dto.getOrderId();
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, 30, TimeUnit.SECONDS);
        if (locked == null || !locked) {
            throw new BusinessException("价格正在被操作，请稍后重试");
        }

        try {
            PickupOrder order = pickupOrderMapper.selectById(dto.getOrderId());
            if (order == null) {
                throw new BusinessException("订单不存在");
            }
            if (!userId.equals(order.getRequesterId())) {
                throw new BusinessException("仅需求者可以修改价格");
            }
            if (!Integer.valueOf(PickupOrderStatus.ACCEPTED.getCode()).equals(order.getStatus())) {
                throw new BusinessException("当前状态不能修改价格");
            }

            order.setProposedPrice(dto.getProposedPrice());
            order.setPriceProposerId(userId);
            order.setPickerPriceConfirmed(0);
            order.setPriceDeadline(LocalDateTime.now().plusHours(24));

            if (dto.getExpectedDeliveryTime() != null) {
                order.setExpectedDeliveryTime(dto.getExpectedDeliveryTime());
                order.setDeliveryDeadline(dto.getExpectedDeliveryTime().plusHours(1));
            }

            pickupOrderMapper.updateById(order);

            // 通知代拿者
            if (order.getPickerId() != null) {
                sendNotification(order.getPickerId(), NotificationType.PICKUP_PRICE_MODIFIED,
                        Map.of("amount", dto.getProposedPrice().toPlainString()), order.getId());
            }
        } finally {
            unlock(lockKey, lockValue);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPrice(Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        String lockKey = RedisConstant.PICKUP_PRICE_LOCK + orderId;
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, 30, TimeUnit.SECONDS);
        if (locked == null || !locked) {
            throw new BusinessException("价格正在被操作，请稍后重试");
        }

        try {
            PickupOrder order = pickupOrderMapper.selectById(orderId);
            if (order == null) {
                throw new BusinessException("订单不存在");
            }
            if (!userId.equals(order.getPickerId())) {
                throw new BusinessException("仅代拿者可以确认价格");
            }
            if (!Integer.valueOf(PickupOrderStatus.ACCEPTED.getCode()).equals(order.getStatus())) {
                throw new BusinessException("当前状态不能确认价格");
            }
            if (Integer.valueOf(1).equals(order.getPickerPriceConfirmed())) {
                throw new BusinessException("已确认过价格");
            }

            order.setPickerPriceConfirmed(1);
            order.setAgreedPrice(order.getProposedPrice());
            order.setStatus(PickupOrderStatus.PRICE_CONFIRMED.getCode());

            // 设置送达截止时间
            if (order.getExpectedDeliveryTime() != null) {
                order.setDeliveryDeadline(order.getExpectedDeliveryTime().plusHours(1));
            }

            pickupOrderMapper.updateById(order);

            // 通知双方
            String amount = order.getAgreedPrice().toPlainString();
            sendNotification(order.getRequesterId(), NotificationType.PICKUP_PRICE_CONFIRMED,
                    Map.of("amount", amount), order.getId());
            sendNotification(order.getPickerId(), NotificationType.PICKUP_PRICE_CONFIRMED,
                    Map.of("amount", amount), order.getId());
        } finally {
            unlock(lockKey, lockValue);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startPickup(Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        PickupOrder order = pickupOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!userId.equals(order.getPickerId())) {
            throw new BusinessException("仅代拿者可以开始代拿");
        }
        if (!Integer.valueOf(PickupOrderStatus.PRICE_CONFIRMED.getCode()).equals(order.getStatus())) {
            throw new BusinessException("当前状态不能开始代拿");
        }

        order.setStatus(PickupOrderStatus.PICKING_UP.getCode());
        pickupOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitEvidence(PickupEvidenceDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        PickupOrder order = pickupOrderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!userId.equals(order.getPickerId())) {
            throw new BusinessException("仅代拿者可以提交证据");
        }
        if (!Integer.valueOf(PickupOrderStatus.PICKING_UP.getCode()).equals(order.getStatus())) {
            throw new BusinessException("当前状态不能提交代拿证据");
        }

        // 保存证据图片为JSON数组
        String imagesJson;
        try {
            imagesJson = objectMapper.writeValueAsString(dto.getEvidenceImages());
        } catch (Exception e) {
            throw new BusinessException("证据图片处理失败");
        }
        order.setEvidenceImages(imagesJson);
        order.setPickerConfirmed(1);
        order.setStatus(PickupOrderStatus.DELIVERED.getCode());
        order.setConfirmDeadline(LocalDateTime.now().plusDays(3));
        pickupOrderMapper.updateById(order);

        // 通知需求者
        sendNotification(order.getRequesterId(), NotificationType.PICKUP_DELIVERED,
                Map.of(), order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        PickupOrder order = pickupOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!userId.equals(order.getRequesterId())) {
            throw new BusinessException("仅需求者可以确认收货");
        }
        if (!Integer.valueOf(PickupOrderStatus.DELIVERED.getCode()).equals(order.getStatus())) {
            throw new BusinessException("当前状态不能确认收货");
        }

        order.setRequesterConfirmed(1);
        order.setStatus(PickupOrderStatus.COMPLETED.getCode());
        order.setCompleteTime(LocalDateTime.now());
        pickupOrderMapper.updateById(order);

        // 增加代拿者信用分
        if (order.getPickerId() != null) {
            User picker = userMapper.selectById(order.getPickerId());
            if (picker != null && picker.getPickupScore() != null) {
                BigDecimal newScore = picker.getPickupScore().add(new BigDecimal("0.1"));
                if (newScore.compareTo(new BigDecimal("5.0")) > 0) {
                    newScore = new BigDecimal("5.0");
                }
                picker.setPickupScore(newScore);
                userMapper.updateById(picker);
            }
        }

        // 通知双方
        sendNotification(order.getRequesterId(), NotificationType.PICKUP_AUTO_CONFIRMED,
                Map.of(), order.getId());
        if (order.getPickerId() != null) {
            sendNotification(order.getPickerId(), NotificationType.PICKUP_AUTO_CONFIRMED,
                    Map.of(), order.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, String cancelReason) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        PickupOrder order = pickupOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 仅待接单(0)和已接单(1)状态可取消
        if (!Integer.valueOf(PickupOrderStatus.PENDING_ACCEPT.getCode()).equals(order.getStatus())
                && !Integer.valueOf(PickupOrderStatus.ACCEPTED.getCode()).equals(order.getStatus())) {
            throw new BusinessException("当前状态不能取消订单");
        }

        // 校验取消权限
        if (!userId.equals(order.getRequesterId())) {
            throw new BusinessException("仅需求者可以取消订单");
        }

        order.setStatus(PickupOrderStatus.CANCELLED.getCode());
        order.setCancelReason(cancelReason);
        order.setCancelBy(userId);
        pickupOrderMapper.updateById(order);

        // 通知代拿者（如果有）
        if (order.getPickerId() != null) {
            sendNotification(order.getPickerId(), NotificationType.PICKUP_TIMEOUT_CANCEL,
                    Map.of(), order.getId());
        }
    }

    private List<Integer> resolveStatuses(String statusGroup) {
        if (statusGroup == null || statusGroup.isBlank()) return null;
        return switch (statusGroup) {
            case "active" -> List.of(0, 1, 2, 3, 4, 8);
            case "done" -> List.of(5, 6, 7);
            default -> {
                try { yield List.of(Integer.parseInt(statusGroup)); }
                catch (NumberFormatException e) { yield null; }
            }
        };
    }

    @Override
    public IPage<PickupOrderListVO> getMyPublished(String statusGroup, Integer page, Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Page<PickupOrderListVO> pageParam = new Page<>(page, pageSize);
        return pickupOrderMapper.getPickupOrderList(pageParam, userId, "requester", resolveStatuses(statusGroup));
    }

    @Override
    public IPage<PickupOrderListVO> getMyPicked(String statusGroup, Integer page, Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Page<PickupOrderListVO> pageParam = new Page<>(page, pageSize);
        return pickupOrderMapper.getPickupOrderList(pageParam, userId, "picker", resolveStatuses(statusGroup));
    }

    @Override
    public IPage<PickupOrderListVO> getAvailableList(Integer page, Integer pageSize) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        Page<PickupOrderListVO> pageParam = new Page<>(page, pageSize);
        return pickupOrderMapper.getAvailableOrderList(pageParam, null, PickupOrderStatus.PENDING_ACCEPT.getCode());
    }

    @Override
    public IPage<PickupPoolVO> getPool(Integer page, Integer pageSize, Long campusId,
                                        String keyword, BigDecimal minPrice, BigDecimal maxPrice,
                                        String expectedTime, String sortBy) {
        Page<PickupPoolVO> pageParam = new Page<>(page, pageSize);
        return pickupOrderMapper.getPoolList(pageParam, campusId, keyword, minPrice, maxPrice, expectedTime, sortBy);
    }

    @Override
    public PickupPoolVO getPoolDetail(Long id) {
        PickupOrder order = pickupOrderMapper.selectById(id);
        if (order == null || Integer.valueOf(1).equals(order.getIsDeleted())) {
            throw new BusinessException("需求不存在");
        }
        if (!Integer.valueOf(PickupOrderStatus.PENDING_ACCEPT.getCode()).equals(order.getStatus())) {
            throw new BusinessException("该需求已被接单或已取消");
        }

        PickupPoolVO vo = new PickupPoolVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setPickupLocation(order.getPickupLocation());
        vo.setPickupDetail(order.getPickupDetail());
        vo.setDeliveryLocation(order.getDeliveryLocation());
        vo.setProposedPrice(order.getProposedPrice());
        vo.setExpectedDeliveryTime(order.getExpectedDeliveryTime());
        vo.setCreateTime(order.getCreateTime());
        vo.setRequesterId(order.getRequesterId());

        User requester = userMapper.selectById(order.getRequesterId());
        if (requester != null) {
            vo.setRequesterNickName(requester.getNickName());
            vo.setRequesterAvatar(requester.getAvatarUrl());
        }
        if (order.getCampusId() != null) {
            com.qingyuan.secondhand.entity.Campus campus = campusMapper.selectById(order.getCampusId());
            if (campus != null) {
                vo.setCampusName(campus.getName());
            }
        }

        return vo;
    }

    // ========== 纠纷操作 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitDispute(PickupDisputeSubmitDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        PickupOrder order = pickupOrderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 校验是否为当事方
        boolean isRequester = userId.equals(order.getRequesterId());
        boolean isPicker = userId.equals(order.getPickerId());
        if (!isRequester && !isPicker) {
            throw new BusinessException("您不是该订单的当事方");
        }

        // 只能在已完成状态下发起纠纷
        if (!Integer.valueOf(PickupOrderStatus.COMPLETED.getCode()).equals(order.getStatus())) {
            throw new BusinessException("订单未完成，不能发起纠纷");
        }

        // 完成时间7天内
        if (order.getCompleteTime() != null && order.getCompleteTime().plusDays(7).isBefore(LocalDateTime.now())) {
            throw new BusinessException("已超过纠纷申诉期限（完成7天内）");
        }

        // 检查是否已有进行中纠纷
        Long existingDispute = pickupDisputeMapper.selectCount(
                new LambdaQueryWrapper<PickupDispute>()
                        .eq(PickupDispute::getOrderId, dto.getOrderId())
                        .in(PickupDispute::getStatus,
                                PickupDisputeStatus.PENDING_RESPONSE.getCode(),
                                PickupDisputeStatus.RESPONDED.getCode())
        );
        if (existingDispute > 0) {
            throw new BusinessException("该订单已有进行中的纠纷");
        }

        // 创建纠纷记录
        PickupDispute dispute = new PickupDispute();
        dispute.setOrderId(dto.getOrderId());
        dispute.setOrderNo(order.getOrderNo());
        dispute.setInitiatorId(userId);
        dispute.setInitiatorRole(isRequester ? 1 : 2);
        dispute.setDisputeType(dto.getDisputeType());
        dispute.setDescription(dto.getDescription());
        dispute.setSubmitTime(LocalDateTime.now());
        dispute.setResponseDeadline(LocalDateTime.now().plusDays(7));
        dispute.setReminderCount(0);
        dispute.setStatus(PickupDisputeStatus.PENDING_RESPONSE.getCode());
        pickupDisputeMapper.insert(dispute);

        // 保存证据
        saveEvidence(dispute.getId(), isRequester ? 1 : 2, dto.getEvidenceMaterials());

        // 更新订单状态
        order.setStatus(PickupOrderStatus.DISPUTE.getCode());
        pickupOrderMapper.updateById(order);

        // 通知对方
        Long responderId = isRequester ? order.getPickerId() : order.getRequesterId();
        if (responderId != null) {
            sendNotification(responderId, NotificationType.PICKUP_DISPUTE_INITIATED,
                    Map.of("orderNo", order.getOrderNo()), order.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void respondDispute(PickupDisputeRespondDTO dto) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        PickupDispute dispute = pickupDisputeMapper.selectById(dto.getDisputeId());
        if (dispute == null) {
            throw new BusinessException("纠纷不存在");
        }
        if (!Integer.valueOf(PickupDisputeStatus.PENDING_RESPONSE.getCode()).equals(dispute.getStatus())) {
            throw new BusinessException("当前状态不能回应");
        }
        if (dispute.getResponseDeadline().isBefore(LocalDateTime.now())) {
            throw new BusinessException("已超过回应期限");
        }

        PickupOrder order = pickupOrderMapper.selectById(dispute.getOrderId());
        if (order == null) {
            throw new BusinessException("关联订单不存在");
        }

        // 校验是否为被申诉方
        boolean isRequester = userId.equals(order.getRequesterId());
        boolean isPicker = userId.equals(order.getPickerId());
        boolean isInitiator = userId.equals(dispute.getInitiatorId());
        if (isInitiator || (!isRequester && !isPicker)) {
            throw new BusinessException("仅被申诉方可以回应");
        }

        // 保存回应证据
        saveEvidence(dispute.getId(), isRequester ? 1 : 2, dto.getResponseMaterials());

        // 更新纠纷状态
        dispute.setResponderId(userId);
        dispute.setResponseDescription(dto.getResponseDescription());
        dispute.setResponseTime(LocalDateTime.now());
        dispute.setStatus(PickupDisputeStatus.RESPONDED.getCode());
        pickupDisputeMapper.updateById(dispute);

        // 通知申诉方
        sendNotification(dispute.getInitiatorId(), NotificationType.PICKUP_DISPUTE_RESPONDED,
                Map.of(), dispute.getOrderId());
    }

    @Override
    public PickupDisputeDetailVO getDisputeDetail(Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        PickupDispute dispute = pickupDisputeMapper.selectOne(
                new LambdaQueryWrapper<PickupDispute>()
                        .eq(PickupDispute::getOrderId, orderId)
                        .orderByDesc(PickupDispute::getCreateTime)
                        .last("LIMIT 1")
        );
        if (dispute == null) {
            throw new BusinessException("纠纷不存在");
        }

        PickupOrder order = pickupOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("关联订单不存在");
        }

        // 校验访问权限
        if (!userId.equals(order.getRequesterId()) && !userId.equals(order.getPickerId())) {
            throw new BusinessException("您不是该订单的当事方");
        }

        // 构建详情VO
        PickupDisputeDetailVO vo = new PickupDisputeDetailVO();
        vo.setId(dispute.getId());
        vo.setOrderId(dispute.getOrderId());
        vo.setOrderNo(dispute.getOrderNo());
        vo.setInitiatorId(dispute.getInitiatorId());
        vo.setInitiatorRole(dispute.getInitiatorRole());
        vo.setDisputeType(dispute.getDisputeType());
        vo.setDescription(dispute.getDescription());
        vo.setSubmitTime(dispute.getSubmitTime());
        vo.setResponderId(dispute.getResponderId());
        vo.setResponseDescription(dispute.getResponseDescription());
        vo.setResponseTime(dispute.getResponseTime());
        vo.setResponseDeadline(dispute.getResponseDeadline());
        vo.setReminderCount(dispute.getReminderCount());
        vo.setStatus(dispute.getStatus());
        vo.setAdminId(dispute.getAdminId());
        vo.setJudgmentResult(dispute.getJudgmentResult());
        vo.setJudgmentDetail(dispute.getJudgmentDetail());
        vo.setPenaltyUserId(dispute.getPenaltyUserId());
        vo.setPenaltyScore(dispute.getPenaltyScore());
        vo.setResolveTime(dispute.getResolveTime());
        vo.setCreateTime(dispute.getCreateTime());
        vo.setRequesterNickName(order.getRequesterId() != null ? getNickName(order.getRequesterId()) : null);
        vo.setPickerNickName(order.getPickerId() != null ? getNickName(order.getPickerId()) : null);

        // 加载双方证据
        List<PickupDisputeEvidence> evidenceList = pickupDisputeEvidenceMapper.selectList(
                new LambdaQueryWrapper<PickupDisputeEvidence>()
                        .eq(PickupDisputeEvidence::getDisputeId, dispute.getId())
                        .orderByAsc(PickupDisputeEvidence::getSortOrder)
        );

        List<PickupDisputeDetailVO.EvidenceVO> initiatorEvidence = new ArrayList<>();
        List<PickupDisputeDetailVO.EvidenceVO> responderEvidence = new ArrayList<>();
        for (PickupDisputeEvidence e : evidenceList) {
            PickupDisputeDetailVO.EvidenceVO evo = new PickupDisputeDetailVO.EvidenceVO();
            evo.setId(e.getId());
            evo.setType(e.getType());
            evo.setUrl(e.getUrl());
            evo.setContent(e.getContent());
            evo.setDescription(e.getDescription());
            evo.setSortOrder(e.getSortOrder());
            if (Integer.valueOf(1).equals(e.getSide())) {
                initiatorEvidence.add(evo);
            } else {
                responderEvidence.add(evo);
            }
        }
        vo.setInitiatorEvidence(initiatorEvidence);
        vo.setResponderEvidence(responderEvidence);

        // 设置申诉方/被申诉方头像
        Long initiatorUserId = dispute.getInitiatorRole() == 1 ? order.getRequesterId() : order.getPickerId();
        Long responderUserId = dispute.getInitiatorRole() == 1 ? order.getPickerId() : order.getRequesterId();
        vo.setInitiatorNickName(initiatorUserId != null ? getNickName(initiatorUserId) : null);
        vo.setResponderNickName(responderUserId != null ? getNickName(responderUserId) : null);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawDispute(Long disputeId) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }

        PickupDispute dispute = pickupDisputeMapper.selectById(disputeId);
        if (dispute == null) {
            throw new BusinessException("纠纷不存在");
        }
        if (!userId.equals(dispute.getInitiatorId())) {
            throw new BusinessException("仅申诉方可以撤销");
        }
        if (!Integer.valueOf(PickupDisputeStatus.PENDING_RESPONSE.getCode()).equals(dispute.getStatus())
                && !Integer.valueOf(PickupDisputeStatus.RESPONDED.getCode()).equals(dispute.getStatus())) {
            throw new BusinessException("当前状态不能撤销");
        }

        dispute.setStatus(PickupDisputeStatus.WITHDRAWN.getCode());
        pickupDisputeMapper.updateById(dispute);

        // 恢复订单状态为已完成
        PickupOrder order = pickupOrderMapper.selectById(dispute.getOrderId());
        if (order != null) {
            order.setStatus(PickupOrderStatus.COMPLETED.getCode());
            pickupOrderMapper.updateById(order);
        }
    }

    // ========== 管理端 ==========

    @Override
    public IPage<PickupOrderListVO> getAdminPickupPage(Integer status, String keyword, Integer page, Integer pageSize) {
        Page<PickupOrderListVO> pageParam = new Page<>(page, pageSize);
        return pickupOrderMapper.getAdminPickupPage(pageParam, status, keyword);
    }

    @Override
    public IPage<PickupDisputeVO> getDisputePage(Integer status, Integer disputeType, Integer page, Integer pageSize) {
        Page<PickupDisputeVO> pageParam = new Page<>(page, pageSize);
        return pickupDisputeMapper.getDisputePage(pageParam, status, disputeType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleDispute(DisputeHandleDTO dto) {
        Long adminId = UserContext.getCurrentUserId();
        if (adminId == null) {
            throw new BusinessException("未登录");
        }

        PickupDispute dispute = pickupDisputeMapper.selectById(dto.getDisputeId());
        if (dispute == null) {
            throw new BusinessException("纠纷不存在");
        }
        if (!Integer.valueOf(PickupDisputeStatus.RESPONDED.getCode()).equals(dispute.getStatus())) {
            throw new BusinessException("当前状态不能裁决");
        }

        dispute.setAdminId(adminId);
        dispute.setJudgmentResult(dto.getJudgmentResult());
        dispute.setJudgmentDetail(dto.getJudgmentDetail());
        dispute.setStatus(PickupDisputeStatus.JUDGED.getCode());
        dispute.setResolveTime(LocalDateTime.now());

        // 处罚
        if (dto.getPenaltyUserId() != null && dto.getPenaltyScore() != null) {
            dispute.setPenaltyUserId(dto.getPenaltyUserId());
            dispute.setPenaltyScore(dto.getPenaltyScore());

            User penaltyUser = userMapper.selectById(dto.getPenaltyUserId());
            if (penaltyUser != null && penaltyUser.getPickupScore() != null) {
                BigDecimal newScore = penaltyUser.getPickupScore().add(dto.getPenaltyScore());
                if (newScore.compareTo(BigDecimal.ZERO) < 0) {
                    newScore = BigDecimal.ZERO;
                }
                penaltyUser.setPickupScore(newScore);
                userMapper.updateById(penaltyUser);
            }
        }

        pickupDisputeMapper.updateById(dispute);

        // 更新订单状态为已取消
        PickupOrder order = pickupOrderMapper.selectById(dispute.getOrderId());
        if (order != null) {
            order.setStatus(PickupOrderStatus.CANCELLED.getCode());
            order.setCancelReason("纠纷裁决");
            order.setCancelBy(adminId);
            pickupOrderMapper.updateById(order);
        }

        // 通知双方
        String resultDesc = PickupDisputeStatus.getByCode(dto.getJudgmentResult()) != null
                ? dto.getJudgmentDetail() : dto.getJudgmentDetail();
        sendNotification(dispute.getInitiatorId(), NotificationType.PICKUP_DISPUTE_JUDGED,
                Map.of("result", resultDesc), dispute.getOrderId());
        if (dispute.getResponderId() != null) {
            sendNotification(dispute.getResponderId(), NotificationType.PICKUP_DISPUTE_JUDGED,
                    Map.of("result", resultDesc), dispute.getOrderId());
        }
    }

    // ========== 工具方法 ==========

    private String generateOrderNo() {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomNum = String.format("%04d", new Random().nextInt(10000));
        return "PD" + timestamp + randomNum;
    }

    private void saveEvidence(Long disputeId, int side, List<PickupDisputeSubmitDTO.EvidenceItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        int sortOrder = 0;
        for (PickupDisputeSubmitDTO.EvidenceItem item : items) {
            PickupDisputeEvidence evidence = new PickupDisputeEvidence();
            evidence.setDisputeId(disputeId);
            evidence.setSide(side);
            evidence.setType("image".equals(item.getType()) ? 1 : 2);
            evidence.setUrl(item.getUrl());
            evidence.setContent(item.getContent());
            evidence.setDescription(item.getDescription());
            evidence.setSortOrder(sortOrder++);
            pickupDisputeEvidenceMapper.insert(evidence);
        }
    }

    private void sendNotification(Long userId, NotificationType type, Map<String, String> params, Long relatedId) {
        if (userId == null) {
            return;
        }
        try {
            notificationService.send(userId, type, params, relatedId, 4, NotificationCategory.PICKUP.getCode());
        } catch (Exception e) {
            log.error("发送通知失败，userId={}, type={}", userId, type.getCode(), e);
        }
    }

    private void unlock(String lockKey, String lockValue) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        try {
            redisTemplate.execute(
                    new DefaultRedisScript<>(script, Long.class),
                    Collections.singletonList(lockKey),
                    lockValue
            );
        } catch (Exception e) {
            log.error("释放Redis锁失败，lockKey={}", lockKey, e);
        }
    }

    private String getNickName(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null ? user.getNickName() : null;
    }

    /**
     * 兜底修复evidenceImages字段的JSON格式。
     * 处理数据库中缺少双引号的脏数据（如 [url1,url2]），修复为合法JSON。
     */
    private String normalizeEvidenceJson(String raw, Long orderId) {
        // 已是合法JSON，直接返回
        try {
            objectMapper.readValue(raw, List.class);
            return raw;
        } catch (Exception ignored) {
        }

        // 尝试修复：去掉方括号，按逗号分割，补双引号
        String cleaned = raw.replaceAll("\\[|\\]", "").trim();
        if (cleaned.isEmpty()) return "[]";

        String[] parts = cleaned.split(",");
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < parts.length; i++) {
            String url = parts[i].trim().replace("\"", "");
            if (i > 0) sb.append(",");
            sb.append("\"").append(url).append("\"");
        }
        sb.append("]");

        // 异步回写数据库，修复脏数据
        try {
            pickupOrderMapper.fixEvidenceImages(orderId, sb.toString());
        } catch (Exception e) {
            log.warn("回写修复evidenceImages失败，orderId={}", orderId, e);
        }

        return sb.toString();
    }
}
