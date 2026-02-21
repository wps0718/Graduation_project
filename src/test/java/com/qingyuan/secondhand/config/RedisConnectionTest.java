package com.qingyuan.secondhand.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisConnectionTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testRedisConnection() {
        String testKey = "test:connection";
        String testValue = "Redis连接测试成功";

        // 写入测试数据
        redisTemplate.opsForValue().set(testKey, testValue, 10, TimeUnit.SECONDS);

        // 读取测试数据
        Object result = redisTemplate.opsForValue().get(testKey);

        // 验证
        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo(testValue);

        // 清理测试数据
        redisTemplate.delete(testKey);
    }

    @Test
    void testRedisTemplateNotNull() {
        assertThat(redisTemplate).isNotNull();
    }
}
