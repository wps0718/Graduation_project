package com.qingyuan.secondhand.service;

import com.qingyuan.secondhand.vo.FollowStatsVO;

public interface FollowService {
    void follow(Long targetUserId);

    void unfollow(Long targetUserId);

    boolean checkFollow(Long targetUserId);

    FollowStatsVO getFollowStats(Long userId);
}

