package com.qingyuan.secondhand.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();

        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equals(request.getMethod())) {
            log.debug("📍 [JWT拦截器] OPTIONS 请求放行: {}", uri);
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        log.info("📍 [JWT拦截器] URI: {}, Authorization: {}", uri, authHeader != null ? "已携带" : "❌ 未携带");

        // 检查 Token 是否存在
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.error("❌ [JWT拦截器] Token 格式错误或不存在");
            writeUnauthorized(response, "未登录");
            return false;
        }

        // 提取 Token
        String token = authHeader.substring(7);
        log.debug("📍 [JWT拦截器] 提取的 Token: {}...", token.substring(0, Math.min(30, token.length())));

        try {
            // 解析 Token
            Claims claims = jwtUtil.parseToken(token);

            // 获取 Token 类型（可能为 null）
            String type = claims.get("type", String.class);
            if (type == null) {
                type = "mini"; // 默认为小程序端
                log.warn("⚠️ [JWT拦截器] Token 中未包含 type 字段，默认设置为 mini");
            }

            // 验证是否为小程序端 Token
            if (!"mini".equals(type)) {
                log.error("❌ [JWT拦截器] Token 类型错误: {}, 期望: mini", type);
                writeUnauthorized(response, "Token 类型错误");
                return false;
            }

            // 获取用户 ID
            String subject = claims.getSubject();
            if (subject == null) {
                log.error("❌ [JWT拦截器] Token 中缺少 subject（用户ID）");
                writeUnauthorized(response, "Token 无效");
                return false;
            }

            Long userId = Long.parseLong(subject);
            log.info("✅ [JWT拦截器] Token 解析成功，用户ID: {}, 类型: {}", userId, type);

            // 设置上下文
            UserContext.setCurrentUserId(userId);
            UserContext.setCurrentUserType(type);
            return true;

        } catch (NumberFormatException e) {
            log.error("❌ [JWT拦截器] 用户ID格式错误: {}", e.getMessage());
            writeUnauthorized(response, "Token 无效");
            return false;
        } catch (Exception e) {
            log.error("❌ [JWT拦截器] Token 解析失败: {}", e.getMessage(), e);
            writeUnauthorized(response, "登录已过期");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        try {
            Result<?> result = Result.error(401, message);
            response.getWriter().write(objectMapper.writeValueAsString(result));
        } catch (IOException ignored) {
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.removeCurrentUserId();
        UserContext.removeCurrentUserType();
    }
}
