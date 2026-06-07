package com.qingyuan.secondhand.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.DisputeHandleDTO;
import com.qingyuan.secondhand.service.PickupService;
import com.qingyuan.secondhand.vo.PickupDisputeDetailVO;
import com.qingyuan.secondhand.vo.PickupDisputeVO;
import com.qingyuan.secondhand.vo.PickupOrderDetailVO;
import com.qingyuan.secondhand.vo.PickupOrderListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理端-代拿快递")
@RestController
@RequestMapping("/admin/pickup")
@RequiredArgsConstructor
public class PickupAdminController {

    private final PickupService pickupService;

    @Operation(summary = "代拿订单分页列表")
    @GetMapping("/page")
    public Result<IPage<PickupOrderListVO>> getPickupPage(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(pickupService.getAdminPickupPage(status, keyword, page, pageSize));
    }

    @Operation(summary = "代拿订单详情")
    @GetMapping("/detail/{id}")
    public Result<PickupOrderDetailVO> getOrderDetail(@PathVariable Long id) {
        return Result.success(pickupService.getOrderDetail(id));
    }

    @Operation(summary = "纠纷分页列表")
    @GetMapping("/dispute/page")
    public Result<IPage<PickupDisputeVO>> getDisputePage(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer disputeType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(pickupService.getDisputePage(status, disputeType, page, pageSize));
    }

    @Operation(summary = "纠纷详情")
    @GetMapping("/dispute/detail/{orderId}")
    public Result<PickupDisputeDetailVO> getDisputeDetail(@PathVariable Long orderId) {
        return Result.success(pickupService.getDisputeDetail(orderId));
    }

    @Operation(summary = "提交裁决")
    @PostMapping("/dispute/handle")
    public Result<Void> handleDispute(@RequestBody @Valid DisputeHandleDTO dto) {
        pickupService.handleDispute(dto);
        return Result.success();
    }
}
