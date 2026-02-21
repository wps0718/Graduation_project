package com.qingyuan.secondhand.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.common.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class AdminJwtInterceptorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void tearDown() {
        UserContext.removeCurrentUserId();
        UserContext.removeCurrentUserType();
    }

    @Test
    void preHandleRejectsWhenTokenMissing() throws Exception {
        JwtUtil jwtUtil = buildJwtUtil();
        AdminJwtInterceptor interceptor = new AdminJwtInterceptor(jwtUtil);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        Result<?> result = objectMapper.readValue(response.getContentAsString(), Result.class);
        assertThat(result.getCode()).isEqualTo(401);
    }

    @Test
    void preHandleAllowsValidToken() {
        JwtUtil jwtUtil = buildJwtUtil();
        AdminJwtInterceptor interceptor = new AdminJwtInterceptor(jwtUtil);
        String token = jwtUtil.createAdminToken(2L, Collections.singletonMap("type", "admin"));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
        assertThat(UserContext.getCurrentUserId()).isEqualTo(2L);
    }

    private JwtUtil buildJwtUtil() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "0123456789abcdef0123456789abcdef");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 60000L);
        ReflectionTestUtils.setField(jwtUtil, "adminExpiration", 60000L);
        return jwtUtil;
    }
}
