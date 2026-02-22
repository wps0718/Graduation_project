package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingyuan.secondhand.entity.ChatSession;
import com.qingyuan.secondhand.vo.ChatSessionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChatSessionMapper extends BaseMapper<ChatSession> {
    List<ChatSessionVO> selectSessionListByUserId(@Param("userId") Long userId);

    int incrementUnread(@Param("userId") Long userId,
                        @Param("peerId") Long peerId,
                        @Param("productId") Long productId);
}
