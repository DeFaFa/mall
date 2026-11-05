package com.macro.mall.config;

import com.macro.mall.auth.AdminAuthInterceptor;
import com.macro.mall.auth.OptionalMemberInterceptor;
import com.macro.mall.auth.PortalAuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AdminAuthInterceptor adminAuthInterceptor;
    private final PortalAuthInterceptor portalAuthInterceptor;
    private final OptionalMemberInterceptor optionalMemberInterceptor;
    private final String[] allowedOrigins;
    private final String uploadDir;

    public WebConfig(AdminAuthInterceptor adminAuthInterceptor,
                     PortalAuthInterceptor portalAuthInterceptor,
                     OptionalMemberInterceptor optionalMemberInterceptor,
                     @Value("${mall.cors.allowed-origins:*}") String[] allowedOrigins,
                     @Value("${mall.upload-dir:./uploads}") String uploadDir) {
        this.adminAuthInterceptor = adminAuthInterceptor;
        this.portalAuthInterceptor = portalAuthInterceptor;
        this.optionalMemberInterceptor = optionalMemberInterceptor;
        this.allowedOrigins = allowedOrigins;
        this.uploadDir = uploadDir;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");
        registry.addInterceptor(optionalMemberInterceptor)
                .addPathPatterns("/portal/product/**");
        registry.addInterceptor(portalAuthInterceptor)
                .addPathPatterns(
                        "/portal/cart/**",
                        "/portal/order/**",
                        "/portal/member/**",
                        "/portal/address/**",
                        "/portal/collection/**",
                        "/portal/attention/**",
                        "/portal/history/**",
                        "/portal/return/**",
                        "/portal/coupon/my",
                        "/portal/coupon/available",
                        "/portal/coupon/receive/**",
                        "/portal/alipay/pay/**")
                .excludePathPatterns("/portal/return/reasons");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + Paths.get(uploadDir).toAbsolutePath().normalize() + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
