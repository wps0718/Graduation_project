package com.qingyuan.secondhand.controller.mini;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.service.NotificationService;
import com.qingyuan.secondhand.dto.NotificationReadDTO;
import com.qingyuan.secondhand.dto.NotificationReadBatchDTO;
import com.qingyuan.secondhand.vo.FavoriteNotificationVO;
import com.qingyuan.secondhand.vo.FollowerNotificationVO;
import com.qingyuan.secondhand.vo.NotificationVO;
import com.qingyuan.secondhand.vo.UnreadCountVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mini/notification")
@RequiredArgsConstructor
public class MiniNotificationController {

    private final NotificationService notificationService;

    @GetMapping("/list")
    public Result<IPage<NotificationVO>> list(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) Integer category) {
        return Result.success(notificationService.getNotificationList(page, pageSize, category));
    }

    @GetMapping("/favorite-list")
    public Result<IPage<FavoriteNotificationVO>> favoriteList(@RequestParam(defaultValue = "1") Integer page,
                                                               @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(notificationService.getFavoriteNotificationList(page, pageSize));
    }

    @GetMapping("/follower-list")
    public Result<IPage<FollowerNotificationVO>> followerList(@RequestParam(defaultValue = "1") Integer page,
                                                               @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(notificationService.getFollowerNotificationList(page, pageSize));
    }

    @PostMapping("/read")
    public Result<Void> read(@RequestBody NotificationReadDTO dto) {
        notificationService.markAsRead(dto.getId());
        return Result.success();
    }

    @PostMapping("/read-batch")
    public Result<Void> readBatch(@RequestBody NotificationReadBatchDTO dto) {
        notificationService.markBatchAsRead(dto.getIds());
        return Result.success();
    }

    @PostMapping("/read-type")
    public Result<Void> readType(@RequestParam Integer type) {
        notificationService.markTypeAsRead(type);
        return Result.success();
    }

    @PostMapping("/read-all")
    public Result<Void> readAll() {
        notificationService.markAllAsRead();
        return Result.success();
    }

    @GetMapping("/unread-count")
    public Result<UnreadCountVO> unreadCount() {
        return Result.success(notificationService.getUnreadCount());
    }
}
