package com.qingyuan.secondhand.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
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

        try {
            redisTemplate.opsForValue().set(testKey, testValue, 10, TimeUnit.SECONDS);
            Object result = redisTemplate.opsForValue().get(testKey);
            assertThat(result).isNotNull();
            assertThat(result.toString()).isEqualTo(testValue);
            redisTemplate.delete(testKey);
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Redis未就绪，跳过连接测试");
        }
    }

    @Test
    void testRedisTemplateNotNull() {
        assertThat(redisTemplate).isNotNull();
    }
}
