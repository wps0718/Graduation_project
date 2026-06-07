package com.qingyuan.secondhand.controller.mini;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.*;
import com.qingyuan.secondhand.service.PickupService;
import com.qingyuan.secondhand.vo.PickupDisputeDetailVO;
import com.qingyuan.secondhand.vo.PickupOrderDetailVO;
import com.qingyuan.secondhand.vo.PickupOrderListVO;
import com.qingyuan.secondhand.vo.PickupPoolVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Tag(name = "小程序-代拿快递")
@RestController
@RequestMapping("/mini/pickup")
@RequiredArgsConstructor
public class PickupController {

    private final PickupService pickupService;

    @Operation(summary = "发布代拿需求")
    @PostMapping("/create")
    public Result<PickupOrderDetailVO> createOrder(@RequestBody @Valid PickupCreateDTO dto) {
        return Result.success(pickupService.createOrder(dto));
    }

    @Operation(summary = "需求池列表（抢单广场）")
    @GetMapping("/pool")
    public Result<IPage<PickupPoolVO>> getPool(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String expectedTime,
            @RequestParam(defaultValue = "urgent") String sortBy) {
        return Result.success(pickupService.getPool(page, pageSize, campusId, keyword, minPrice, maxPrice, expectedTime, sortBy));
    }

    @Operation(summary = "需求详情预览（接单前）")
    @GetMapping("/pool/{id}")
    public Result<PickupPoolVO> getPoolDetail(@PathVariable Long id) {
        return Result.success(pickupService.getPoolDetail(id));
    }

    @Operation(summary = "代拿需求列表（待接单）")
    @GetMapping("/list")
    public Result<IPage<PickupOrderListVO>> getAvailableList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(pickupService.getAvailableList(page, pageSize));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/detail/{id}")
    public Result<PickupOrderDetailVO> getOrderDetail(@PathVariable Long id) {
        return Result.success(pickupService.getOrderDetail(id));
    }

    @Operation(summary = "代拿者接单")
    @PostMapping("/accept")
    public Result<Void> acceptOrder(@RequestBody @Valid PickupOrderIdDTO dto) {
        pickupService.acceptOrder(dto.getOrderId());
        return Result.success();
    }

    @Operation(summary = "需求者修改价格")
    @PostMapping("/modify-price")
    public Result<Void> modifyPrice(@RequestBody @Valid PickupPriceModifyDTO dto) {
        pickupService.modifyPrice(dto);
        return Result.success();
    }

    @Operation(summary = "代拿者确认价格")
    @PostMapping("/confirm-price")
    public Result<Void> confirmPrice(@RequestBody @Valid PickupOrderIdDTO dto) {
        pickupService.confirmPrice(dto.getOrderId());
        return Result.success();
    }

    @Operation(summary = "开始代拿")
    @PostMapping("/start")
    public Result<Void> startPickup(@RequestBody @Valid PickupOrderIdDTO dto) {
        pickupService.startPickup(dto.getOrderId());
        return Result.success();
    }

    @Operation(summary = "提交代拿证据")
    @PostMapping("/submit-evidence")
    public Result<Void> submitEvidence(@RequestBody @Valid PickupEvidenceDTO dto) {
        pickupService.submitEvidence(dto);
        return Result.success();
    }

    @Operation(summary = "需求者确认收货")
    @PostMapping("/confirm-receive")
    public Result<Void> confirmReceive(@RequestBody @Valid PickupOrderIdDTO dto) {
        pickupService.confirmReceive(dto.getOrderId());
        return Result.success();
    }

    @Operation(summary = "取消订单")
    @PostMapping("/cancel")
    public Result<Void> cancelOrder(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String cancelReason = body.get("cancelReason") != null ? body.get("cancelReason").toString() : null;
        pickupService.cancelOrder(orderId, cancelReason);
        return Result.success();
    }

    @Operation(summary = "我发布的需求列表")
    @GetMapping("/my-published")
    public Result<IPage<PickupOrderListVO>> getMyPublished(
            @RequestParam(required = false) String statusGroup,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(pickupService.getMyPublished(statusGroup, page, pageSize));
    }

    @Operation(summary = "我接单的代拿列表")
    @GetMapping("/my-picked")
    public Result<IPage<PickupOrderListVO>> getMyPicked(
            @RequestParam(required = false) String statusGroup,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(pickupService.getMyPicked(statusGroup, page, pageSize));
    }

    // ========== 纠纷接口 ==========

    @Operation(summary = "发起纠纷申诉")
    @PostMapping("/dispute/submit")
    public Result<Void> submitDispute(@RequestBody @Valid PickupDisputeSubmitDTO dto) {
        pickupService.submitDispute(dto);
        return Result.success();
    }

    @Operation(summary = "被申诉方回应举证")
    @PostMapping("/dispute/respond")
    public Result<Void> respondDispute(@RequestBody @Valid PickupDisputeRespondDTO dto) {
        pickupService.respondDispute(dto);
        return Result.success();
    }

    @Operation(summary = "查看纠纷详情")
    @GetMapping("/dispute/detail/{orderId}")
    public Result<PickupDisputeDetailVO> getDisputeDetail(@PathVariable Long orderId) {
        return Result.success(pickupService.getDisputeDetail(orderId));
    }

    @Operation(summary = "撤销申诉")
    @PostMapping("/dispute/withdraw")
    public Result<Void> withdrawDispute(@RequestBody @Valid PickupOrderIdDTO dto) {
        pickupService.withdrawDispute(dto.getOrderId());
        return Result.success();
    }
}
