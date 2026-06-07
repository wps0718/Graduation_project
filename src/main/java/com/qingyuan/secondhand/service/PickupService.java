package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qingyuan.secondhand.dto.*;
import com.qingyuan.secondhand.vo.*;

import java.math.BigDecimal;

public interface PickupService {

    // 需求池
    IPage<PickupPoolVO> getPool(Integer page, Integer pageSize, Long campusId,
                                String keyword, BigDecimal minPrice, BigDecimal maxPrice,
                                String expectedTime, String sortBy);

    PickupPoolVO getPoolDetail(Long id);

    // 订单操作
    PickupOrderDetailVO createOrder(PickupCreateDTO dto);

    IPage<PickupOrderListVO> getOrderList(String role, Integer status, Integer page, Integer pageSize);

    PickupOrderDetailVO getOrderDetail(Long id);

    void acceptOrder(Long orderId);

    void modifyPrice(PickupPriceModifyDTO dto);

    void confirmPrice(Long orderId);

    void startPickup(Long orderId);

    void submitEvidence(PickupEvidenceDTO dto);

    void confirmReceive(Long orderId);

    void cancelOrder(Long orderId, String cancelReason);

    IPage<PickupOrderListVO> getMyPublished(String statusGroup, Integer page, Integer pageSize);

    IPage<PickupOrderListVO> getMyPicked(String statusGroup, Integer page, Integer pageSize);

    IPage<PickupOrderListVO> getAvailableList(Integer page, Integer pageSize);

    // 纠纷操作
    void submitDispute(PickupDisputeSubmitDTO dto);

    void respondDispute(PickupDisputeRespondDTO dto);

    PickupDisputeDetailVO getDisputeDetail(Long orderId);

    void withdrawDispute(Long disputeId);

    // 管理端
    IPage<PickupOrderListVO> getAdminPickupPage(Integer status, String keyword, Integer page, Integer pageSize);

    IPage<PickupDisputeVO> getDisputePage(Integer status, Integer disputeType, Integer page, Integer pageSize);

    void handleDispute(DisputeHandleDTO dto);
}
