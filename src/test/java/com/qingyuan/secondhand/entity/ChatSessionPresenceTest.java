package com.qingyuan.secondhand.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatSessionPresenceTest {

    @Test
    void chatSessionEntityShouldExistWithExpectedFields() throws Exception {
        Class<?> entityClass = Class.forName("com.qingyuan.secondhand.entity.ChatSession");
        TableName tableName = entityClass.getAnnotation(TableName.class);
        assertNotNull(tableName);
        assertEquals("chat_session", tableName.value());
        assertNotNull(entityClass.getDeclaredField("id"));
        assertNotNull(entityClass.getDeclaredField("userId"));
        assertNotNull(entityClass.getDeclaredField("peerId"));
        assertNotNull(entityClass.getDeclaredField("productId"));
        assertNotNull(entityClass.getDeclaredField("lastMsg"));
        assertNotNull(entityClass.getDeclaredField("unread"));
        assertNotNull(entityClass.getDeclaredField("lastTime"));
        assertNotNull(entityClass.getDeclaredField("createTime"));
        assertNotNull(entityClass.getDeclaredField("updateTime"));
    }

    @Test
    void chatSessionMapperShouldExist() throws Exception {
        Class<?> mapperClass = Class.forName("com.qingyuan.secondhand.mapper.ChatSessionMapper");
        assertTrue(BaseMapper.class.isAssignableFrom(mapperClass));
    }
}
