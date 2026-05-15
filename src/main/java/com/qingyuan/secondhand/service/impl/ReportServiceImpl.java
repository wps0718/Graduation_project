package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.enums.ProductStatus;
import com.qingyuan.secondhand.common.enums.UserStatus;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.common.util.PhoneUtil;
import com.qingyuan.secondhand.dto.ReportHandleDTO;
import com.qingyuan.secondhand.dto.ReportSubmitDTO;
import com.qingyuan.secondhand.entity.Product;
import com.qingyuan.secondhand.entity.Report;
import com.qingyuan.secondhand.entity.TradeOrder;
import com.qingyuan.secondhand.entity.User;
import com.qingyuan.secondhand.mapper.ProductMapper;
import com.qingyuan.secondhand.mapper.ReportMapper;
import com.qingyuan.secondhand.mapper.TradeOrderMapper;
import com.qingyuan.secondhand.mapper.UserMapper;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.service.ReportService;
import com.qingyuan.secondhand.vo.ReportDetailVO;
import com.qingyuan.secondhand.vo.ReportPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.qingyuan.secondhand.common.util.ImageJsonUtil.parseCoverImage;
import static com.qingyuan.secondhand.common.util.ImageJsonUtil.parseImages;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    private final ReportMapper reportMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final TradeOrderMapper tradeOrderMapper;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    public void submitReport(ReportSubmitDTO dto) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("未登录");
        }
        if (dto == null || dto.getTargetId() == null || dto.getTargetType() == null) {
            throw new BusinessException("举报参数不完整");
        }
        if (!Integer.valueOf(1).equals(dto.getTargetType()) && !Integer.valueOf(2).equals(dto.getTargetType())) {
            throw new BusinessException("目标类型不正确");
        }
        Product cachedProduct = null;
        User cachedUser = null;
        if (Integer.valueOf(1).equals(dto.getTargetType())) {
            cachedProduct = productMapper.selectById(dto.getTargetId());
            if (cachedProduct == null || Integer.valueOf(1).equals(cachedProduct.getIsDeleted())) {
                throw new BusinessException("举报目标不存在");
            }
            if (currentUserId.equals(cachedProduct.getUserId())) {
                throw new BusinessException("不能举报自己");
            }
        } else {
            if (currentUserId.equals(dto.getTargetId())) {
                throw new BusinessException("不能举报自己");
            }
            cachedUser = userMapper.selectById(dto.getTargetId());
            if (cachedUser == null) {
                throw new BusinessException("举报目标不存在");
            }
        }
        Report existing = reportMapper.selectOne(new LambdaQueryWrapper<Report>()
                .eq(Report::getReporterId, currentUserId)
                .eq(Report::getTargetId, dto.getTargetId())
                .eq(Report::getTargetType, dto.getTargetType()));
        if (existing != null) {
            throw new BusinessException("您已举报过该目标");
        }
        Report report = new Report();
        report.setReporterId(currentUserId);
        report.setTargetId(dto.getTargetId());
        report.setTargetType(dto.getTargetType());
        report.setReasonType(dto.getReasonType());
        report.setDescription(dto.getDescription());
        report.setStatus(0);
        // 保存被举报目标快照（复用验证阶段的查询结果）
        if (Integer.valueOf(1).equals(dto.getTargetType())) {
            if (cachedProduct != null) {
                report.setTargetTitle(cachedProduct.getTitle());
                report.setTargetCoverImage(parseCoverImage(cachedProduct.getImages()));
            }
        } else {
            if (cachedUser != null) {
                report.setTargetTitle(cachedUser.getNickName());
                report.setTargetCoverImage(cachedUser.getAvatarUrl());
            }
        }
        int inserted = reportMapper.insert(report);
        if (inserted <= 0) {
            throw new BusinessException("提交举报失败");
        }
    }

    @Override
    public IPage<ReportPageVO> getReportPage(Integer page, Integer pageSize, Integer status, Integer targetType) {
        if (page == null || pageSize == null) {
            throw new BusinessException("分页参数不能为空");
        }
        Page<ReportPageVO> pageObj = new Page<>(page, pageSize);
        IPage<ReportPageVO> result = reportMapper.getReportPage(pageObj, status, targetType);
        if (result != null && result.getRecords() != null) {
            for (ReportPageVO record : result.getRecords()) {
                if (Integer.valueOf(1).equals(record.getTargetType())) {
                    // 解析商品封面（用于详情等场景）
                    record.setProductCoverImage(parseCoverImage(record.getProductCoverImageJson()));
                    // 优先使用快照标题，无快照则回退到商品标题
                    if (!StringUtils.hasText(record.getTargetTitle())) {
                        record.setTargetTitle(record.getProductTitle());
                    }
                    // 优先使用快照封面，无快照则回退到商品封面
                    if (!StringUtils.hasText(record.getTargetCoverImage())) {
                        record.setTargetCoverImage(record.getProductCoverImage());
                    }
                } else if (Integer.valueOf(2).equals(record.getTargetType())) {
                    // 用户举报：优先使用快照，无快照则回退到用户信息
                    if (!StringUtils.hasText(record.getTargetTitle())) {
                        record.setTargetTitle(record.getTargetUserNickName());
                    }
                    if (!StringUtils.hasText(record.getTargetCoverImage())) {
                        record.setTargetCoverImage(record.getTargetUserAvatarUrl());
                    }
                }
            }
        }
        return result == null ? new Page<>(page, pageSize) : result;
    }

    @Override
    public ReportDetailVO getReportDetail(Long id) {
        if (id == null) {
            throw new BusinessException("举报ID不能为空");
        }
        ReportDetailVO detail = reportMapper.getReportDetailById(id);
        if (detail == null) {
            throw new BusinessException("举报记录不存在");
        }
        detail.setProductImages(parseImages(detail.getProductImagesJson()));
        detail.setReporterPhone(PhoneUtil.maskPhone(detail.getReporterPhone()));
        detail.setProductUserPhone(PhoneUtil.maskPhone(detail.getProductUserPhone()));
        detail.setTargetUserPhone(PhoneUtil.maskPhone(detail.getTargetUserPhone()));
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleReport(ReportHandleDTO dto, Long handlerId) {
        if (handlerId == null) {
            throw new BusinessException("未登录");
        }
        if (dto == null || dto.getReportId() == null) {
            throw new BusinessException("举报ID不能为空");
        }
        Report report = reportMapper.selectById(dto.getReportId());
        if (report == null) {
            throw new BusinessException("举报记录不存在");
        }
        if (!Integer.valueOf(0).equals(report.getStatus())) {
            throw new BusinessException("该举报已处理");
        }
        LocalDateTime now = LocalDateTime.now();
        String action = dto.getAction();
        if ("off_shelf".equals(action)) {
            handleOffShelf(report, dto, handlerId, now);
        } else if ("warn".equals(action)) {
            handleWarn(report, dto, handlerId, now);
        } else if ("ban".equals(action)) {
            handleBan(report, dto, handlerId, now);
        } else if ("ignore".equals(action)) {
            handleIgnore(report, dto, handlerId, now);
        } else {
            throw new BusinessException("处理动作不正确");
        }
        int updated = reportMapper.updateById(report);
        if (updated <= 0) {
            throw new BusinessException("处理举报失败");
        }
    }

    private void handleOffShelf(Report report, ReportHandleDTO dto, Long handlerId, LocalDateTime now) {
        if (!Integer.valueOf(1).equals(report.getTargetType())) {
            throw new BusinessException("只有商品举报才能执行下架操作");
        }
        Product product = productMapper.selectById(report.getTargetId());
        if (product == null || Integer.valueOf(1).equals(product.getIsDeleted())) {
            throw new BusinessException("商品不存在");
        }
        Product update = new Product();
        update.setId(product.getId());
        update.setStatus(ProductStatus.OFF_SHELF.getCode());
        int productUpdated = productMapper.updateById(update);
        if (productUpdated <= 0) {
            throw new BusinessException("商品下架失败");
        }
        report.setStatus(1);
        report.setHandleResult(StringUtils.hasText(dto.getHandleResult()) ? dto.getHandleResult() : "商品已强制下架");
        report.setHandlerId(handlerId);
        report.setHandleTime(now);
        notificationService.send(
                product.getUserId(),
                2,
                "商品被举报下架",
                "您的商品《" + product.getTitle() + "》因被举报已下架",
                product.getId(),
                1,
                2
        );
    }

    private void handleWarn(Report report, ReportHandleDTO dto, Long handlerId, LocalDateTime now) {
        Long targetUserId = resolveTargetUserId(report);
        if (targetUserId == null) {
            throw new BusinessException("举报目标不存在");
        }
        report.setStatus(1);
        report.setHandleResult(StringUtils.hasText(dto.getHandleResult()) ? dto.getHandleResult() : "已警告用户");
        report.setHandlerId(handlerId);
        report.setHandleTime(now);
        notificationService.send(
                targetUserId,
                2,
                "您收到了警告",
                "您因违规行为收到警告，请注意遵守平台规则",
                report.getId(),
                4,
                2
        );
    }

    private void handleBan(Report report, ReportHandleDTO dto, Long handlerId, LocalDateTime now) {
        Long targetUserId = resolveTargetUserId(report);
        if (targetUserId == null) {
            throw new BusinessException("举报目标不存在");
        }
        User user = userMapper.selectById(targetUserId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setStatus(UserStatus.BANNED.getCode());
        String handleReason = StringUtils.hasText(dto.getHandleResult()) ? dto.getHandleResult() : "因举报被封禁";
        updateUser.setBanReason(handleReason);
        updateUser.setUpdateTime(now);
        int updatedUser = userMapper.updateById(updateUser);
        if (updatedUser <= 0) {
            throw new BusinessException("封禁用户失败");
        }
        userMapper.offShelfAllProducts(targetUserId);
        // 批量取消用户的活跃订单（3N+1 → 4 查询）
        tradeOrderMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<TradeOrder>()
                .eq(TradeOrder::getStatus, 1)
                .and(w -> w.eq(TradeOrder::getBuyerId, targetUserId)
                        .or()
                        .eq(TradeOrder::getSellerId, targetUserId))
                .set(TradeOrder::getStatus, 5)
                .set(TradeOrder::getCancelBy, handlerId)
                .set(TradeOrder::getCancelReason, "用户被封禁，订单自动取消")
                .set(TradeOrder::getUpdateTime, now));
        // 批量恢复相关商品为在售状态
        List<Long> productIds = tradeOrderMapper.selectList(new LambdaQueryWrapper<TradeOrder>()
                        .select(TradeOrder::getProductId)
                        .eq(TradeOrder::getStatus, 5)
                        .eq(TradeOrder::getCancelBy, handlerId)
                        .and(w -> w.eq(TradeOrder::getBuyerId, targetUserId)
                                .or()
                                .eq(TradeOrder::getSellerId, targetUserId)))
                .stream()
                .map(TradeOrder::getProductId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        if (!productIds.isEmpty()) {
            productMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product>()
                    .in(Product::getId, productIds)
                    .eq(Product::getIsDeleted, 0)
                    .set(Product::getStatus, ProductStatus.ON_SALE.getCode()));
        }
        report.setStatus(1);
        report.setHandleResult(StringUtils.hasText(dto.getHandleResult()) ? dto.getHandleResult() : "用户已封禁");
        report.setHandlerId(handlerId);
        report.setHandleTime(now);
        notificationService.send(
                targetUserId,
                2,
                "账号已被封禁",
                "您的账号因严重违规已被封禁，封禁原因：" + handleReason,
                report.getId(),
                4,
                2
        );
    }

    private void handleIgnore(Report report, ReportHandleDTO dto, Long handlerId, LocalDateTime now) {
        report.setStatus(2);
        report.setHandleResult(StringUtils.hasText(dto.getHandleResult()) ? dto.getHandleResult() : "举报已忽略");
        report.setHandlerId(handlerId);
        report.setHandleTime(now);
    }

    private Long resolveTargetUserId(Report report) {
        if (Integer.valueOf(1).equals(report.getTargetType())) {
            Product product = productMapper.selectById(report.getTargetId());
            if (product == null || Integer.valueOf(1).equals(product.getIsDeleted())) {
                return null;
            }
            return product.getUserId();
        }
        if (Integer.valueOf(2).equals(report.getTargetType())) {
            return report.getTargetId();
        }
        return null;
    }

    private String toJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            throw new BusinessException("证据处理失败");
        }
    }
}
