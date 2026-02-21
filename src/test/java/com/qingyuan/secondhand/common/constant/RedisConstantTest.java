package com.qingyuan.secondhand.common.constant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedisConstantTest {

    @Test
    void keysMatchSpec() {
        assertThat(RedisConstant.SMS_CODE).isEqualTo("sms:code:");
        assertThat(RedisConstant.SMS_LIMIT).isEqualTo("sms:limit:");
        assertThat(RedisConstant.SMS_DAILY).isEqualTo("sms:daily:");
        assertThat(RedisConstant.USER_TOKEN).isEqualTo("user:token:");
        assertThat(RedisConstant.ADMIN_TOKEN).isEqualTo("admin:token:");
        assertThat(RedisConstant.PRODUCT_VIEW).isEqualTo("product:view:");
        assertThat(RedisConstant.CATEGORY_LIST).isEqualTo("category:list");
        assertThat(RedisConstant.CAMPUS_LIST).isEqualTo("campus:list");
        assertThat(RedisConstant.COLLEGE_LIST).isEqualTo("college:list");
        assertThat(RedisConstant.BANNER_LIST).isEqualTo("banner:list:");
        assertThat(RedisConstant.SEARCH_HOT).isEqualTo("search:hot");
        assertThat(RedisConstant.USER_STATS).isEqualTo("user:stats:");
        assertThat(RedisConstant.LOGIN_FAIL).isEqualTo("login:fail:");
        assertThat(RedisConstant.PRODUCT_LOCK).isEqualTo("product:lock:");
    }
}
