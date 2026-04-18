package com.qingyuan.secondhand.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
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
        // 放行 OPTIONS 预检请求，让 CORS 配置处理
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                Claims claims = jwtUtil.parseToken(token);
                String type = claims.get("type", String.class);
                if (!"admin".equals(type)) {
                    writeUnauthorized(response);
                    return false;
                }
                Long adminId = Long.parseLong(claims.getSubject());
                UserContext.setCurrentUserId(adminId);
                UserContext.setCurrentUserType(type);
                return true;
            } catch (Exception e) {
                writeUnauthorized(response);
                return false;
            }
        }
        writeUnauthorized(response);
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.removeCurrentUserId();
        UserContext.removeCurrentUserType();
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
