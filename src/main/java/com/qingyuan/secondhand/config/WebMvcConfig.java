package com.qingyuan.secondhand.config;

import com.qingyuan.secondhand.common.interceptor.AdminJwtInterceptor;
import com.qingyuan.secondhand.common.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final AdminJwtInterceptor adminJwtInterceptor;

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.url-prefix}")
    private String urlPrefix;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/mini/**")
                .excludePathPatterns(
                        // 登录相关
                        "/mini/user/login",
                        "/mini/user/wx-login",
                        "/mini/user/sms/send",
                        "/mini/user/sms-login",
                        // 用户公开接口
                        "/mini/user/profile/**",
                        "/mini/common/**",
                        // 校区相关（首页必需）
                        "/mini/campus/list",
                        "/mini/campus/meeting-points/**",
                        // 分类相关（首页必需）
                        "/mini/category/list",
                        // Banner（首页必需）
                        "/mini/banner/list",
                        // 商品公开接口（首页必需）
                        "/mini/product/list",
                        "/mini/product/detail/**",
                        // 搜索相关（首页必需）
                        "/mini/search/hot-keywords",
                        // 收藏相关
                        "/mini/favorite/check/**",
                        // 学院列表
                        "/mini/college/list"
                );

        registry.addInterceptor(adminJwtInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login", "/admin/employee/login", "/admin/common/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadPath).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler(urlPrefix + "**")
                .addResourceLocations(location);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的源（前端地址）- 生产环境建议配置具体域名
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000",
                        "http://localhost:5173", "http://127.0.0.1:5173")
                // 允许的 HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 允许携带凭证（cookies、authorization header）
                .allowCredentials(true)
                // 预检请求缓存时间（秒）
                .maxAge(3600);
    }
}
