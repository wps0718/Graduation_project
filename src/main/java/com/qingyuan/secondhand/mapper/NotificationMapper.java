package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingyuan.secondhand.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    int insertBatch(@Param("list") List<Notification> list);
}
