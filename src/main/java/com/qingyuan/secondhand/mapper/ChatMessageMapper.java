package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingyuan.secondhand.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    List<ChatMessage> selectMessagesBySessionKey(@Param("sessionKey") String sessionKey,
                                                 @Param("offset") int offset,
                                                 @Param("limit") int limit);

    int selectMessageCount(@Param("sessionKey") String sessionKey);

    int markAsRead(@Param("receiverId") Long receiverId,
                   @Param("sessionKey") String sessionKey);
}
