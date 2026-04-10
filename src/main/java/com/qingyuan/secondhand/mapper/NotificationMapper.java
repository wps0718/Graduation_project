package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.entity.Notification;
import com.qingyuan.secondhand.vo.FavoriteNotificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    int insertBatch(@Param("list") List<Notification> list);

    IPage<FavoriteNotificationVO> selectFavoriteNotifications(Page<FavoriteNotificationVO> page, @Param("userId") Long userId);
}
