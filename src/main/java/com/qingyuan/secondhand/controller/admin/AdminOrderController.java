package com.qingyuan.secondhand.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.service.TradeOrderService;
import com.qingyuan.secondhand.vo.AdminOrderPageVO;
import com.qingyuan.secondhand.vo.OrderDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final TradeOrderService tradeOrderService;

    @GetMapping("/page")
    public Result<IPage<AdminOrderPageVO>> page(@RequestParam Integer page,
                                                @RequestParam Integer pageSize,
                                                @RequestParam(required = false) Integer status) {
        return Result.success(tradeOrderService.getAdminOrderPage(page, pageSize, status));
    }

    @GetMapping("/detail/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.success(tradeOrderService.getAdminOrderDetail(id));
    }
}
