package com.qingyuan.secondhand.controller.mini;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.OrderCancelDTO;
import com.qingyuan.secondhand.dto.OrderCreateDTO;
import com.qingyuan.secondhand.dto.OrderIdDTO;
import com.qingyuan.secondhand.service.TradeOrderService;
import com.qingyuan.secondhand.vo.OrderCreateVO;
import com.qingyuan.secondhand.vo.OrderDetailVO;
import com.qingyuan.secondhand.vo.OrderListVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mini/order")
@RequiredArgsConstructor
public class MiniOrderController {

    private final TradeOrderService tradeOrderService;

    @PostMapping("/create")
    public Result<OrderCreateVO> createOrder(@RequestBody @Valid OrderCreateDTO dto) {
        return Result.success(tradeOrderService.createOrder(dto));
    }

    @GetMapping("/list")
    public Result<IPage<OrderListVO>> getOrderList(
            @RequestParam String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(tradeOrderService.getOrderList(role, status, page, pageSize));
    }

    @GetMapping("/detail/{id}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable Long id) {
        return Result.success(tradeOrderService.getOrderDetail(id));
    }

    @PostMapping("/confirm")
    public Result<Void> confirm(@RequestBody @Valid OrderIdDTO dto) {
        tradeOrderService.confirmOrder(dto.getOrderId());
        return Result.success();
    }

    @PostMapping("/cancel")
    public Result<Void> cancel(@RequestBody @Valid OrderCancelDTO dto) {
        tradeOrderService.cancelOrder(dto.getOrderId(), dto.getCancelReason());
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody @Valid OrderIdDTO dto) {
        tradeOrderService.deleteOrder(dto.getOrderId());
        return Result.success();
    }
}
