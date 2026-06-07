package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.entity.PickupOrder;
import com.qingyuan.secondhand.vo.PickupOrderDetailVO;
import com.qingyuan.secondhand.vo.PickupOrderListVO;
import com.qingyuan.secondhand.vo.PickupPoolVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface PickupOrderMapper extends BaseMapper<PickupOrder> {

    IPage<PickupOrderListVO> getPickupOrderList(Page<PickupOrderListVO> page,
                                                  @Param("userId") Long userId,
                                                  @Param("role") String role,
                                                  @Param("statuses") List<Integer> statuses);

    IPage<PickupOrderListVO> getAvailableOrderList(Page<PickupOrderListVO> page,
                                                     @Param("campusId") Long campusId,
                                                     @Param("status") Integer status);

    IPage<PickupPoolVO> getPoolList(Page<PickupPoolVO> page,
                                     @Param("campusId") Long campusId,
                                     @Param("keyword") String keyword,
                                     @Param("minPrice") BigDecimal minPrice,
                                     @Param("maxPrice") BigDecimal maxPrice,
                                     @Param("expectedTime") String expectedTime,
                                     @Param("sortBy") String sortBy);

    PickupOrderDetailVO getOrderDetail(@Param("id") Long id);

    IPage<PickupOrderListVO> getAdminPickupPage(Page<PickupOrderListVO> page,
                                                  @Param("status") Integer status,
                                                  @Param("keyword") String keyword);

    void fixEvidenceImages(@Param("id") Long id, @Param("evidenceImages") String evidenceImages);
}
