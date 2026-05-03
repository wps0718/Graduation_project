package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.entity.TradeOrder;
import com.qingyuan.secondhand.vo.AdminOrderPageVO;
import com.qingyuan.secondhand.vo.OrderDetailVO;
import com.qingyuan.secondhand.vo.OrderListVO;
import com.qingyuan.secondhand.vo.RelatedOrderVO;
import org.apache.ibatis.annotations.Param;

public interface TradeOrderMapper extends BaseMapper<TradeOrder> {
    Page<OrderListVO> getOrderList(Page<OrderListVO> page,
                                   @Param("userId") Long userId,
                                   @Param("role") String role,
                                   @Param("status") Integer status);

    OrderDetailVO getOrderDetail(@Param("id") Long id);

    Page<AdminOrderPageVO> getAdminOrderPage(Page<AdminOrderPageVO> page,
                                             @Param("status") Integer status);

    Page<RelatedOrderVO> getRelatedOrdersByProductId(Page<RelatedOrderVO> page,
                                                      @Param("productId") Long productId);
}
