package com.qingyuan.secondhand.controller.mini;

import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.FollowUserDTO;
import com.qingyuan.secondhand.service.FollowService;
import com.qingyuan.secondhand.vo.FollowStatsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mini/follow")
@RequiredArgsConstructor
public class MiniFollowController {

    private final FollowService followService;

    @PostMapping("/follow")
    public Result<Void> follow(@RequestBody @Valid FollowUserDTO dto) {
        followService.follow(dto.getUserId());
        return Result.success();
    }

    @PostMapping("/unfollow")
    public Result<Void> unfollow(@RequestBody @Valid FollowUserDTO dto) {
        followService.unfollow(dto.getUserId());
        return Result.success();
    }

    @GetMapping("/check/{userId}")
    public Result<Boolean> check(@PathVariable Long userId) {
        return Result.success(followService.checkFollow(userId));
    }

    @GetMapping("/stats/{userId}")
    public Result<FollowStatsVO> stats(@PathVariable Long userId) {
        return Result.success(followService.getFollowStats(userId));
    }
}

