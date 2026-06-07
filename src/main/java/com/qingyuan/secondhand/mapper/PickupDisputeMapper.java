package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.entity.PickupDispute;
import com.qingyuan.secondhand.vo.PickupDisputeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PickupDisputeMapper extends BaseMapper<PickupDispute> {

    IPage<PickupDisputeVO> getDisputePage(Page<PickupDisputeVO> page,
                                           @Param("status") Integer status,
                                           @Param("disputeType") Integer disputeType);
}
