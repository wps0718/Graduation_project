package com.qingyuan.secondhand.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AdminJwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
            // In admin context, we might store admin ID instead of user ID, but using same context for now or different one if needed.
            // Requirement said "parse admin info", assume it's also an ID.
            Long adminId = jwtUtil.getUserId(token);
            if (adminId != null) {
                UserContext.setCurrentUserId(adminId);
                return true;
            }
        }
        writeUnauthorized(response);
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.removeCurrentUserId();
    }

    private void writeUnauthorized(HttpServletResponse response) {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        try {
            Result<?> result = Result.error(401, "Unauthorized");
            response.getWriter().write(new ObjectMapper().writeValueAsString(result));
        } catch (IOException ignored) {
        }
    }
}
