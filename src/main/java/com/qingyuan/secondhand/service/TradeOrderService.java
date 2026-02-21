package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.dto.OrderCreateDTO;
import com.qingyuan.secondhand.entity.TradeOrder;
import com.qingyuan.secondhand.vo.AdminOrderPageVO;
import com.qingyuan.secondhand.vo.OrderCreateVO;
import com.qingyuan.secondhand.vo.OrderDetailVO;
import com.qingyuan.secondhand.vo.OrderListVO;

public interface TradeOrderService extends IService<TradeOrder> {
    OrderCreateVO createOrder(OrderCreateDTO dto);

    IPage<OrderListVO> getOrderList(String role, Integer status, Integer pageNum, Integer pageSize);

    OrderDetailVO getOrderDetail(Long id);

    void confirmOrder(Long orderId);

    void cancelOrder(Long orderId, String cancelReason);

    void deleteOrder(Long orderId);

    IPage<AdminOrderPageVO> getAdminOrderPage(Integer page, Integer pageSize, Integer status);

    OrderDetailVO getAdminOrderDetail(Long id);
}
