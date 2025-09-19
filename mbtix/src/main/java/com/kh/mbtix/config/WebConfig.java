package com.kh.mbtix.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/chatbot_profiles/**")
//            .allowedOrigins("http://localhost:5173")
//            .allowedMethods("GET", "HEAD")
//            .allowedHeaders("*");
//    }

    // 정적 리소스 핸들러 추가
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // '/mbtix_storage/images/**' URL 패턴이 요청될 경우,
        // 'file:///C:/mbtix_storage/images/' 디렉토리에서 리소스를 찾도록 설정합니다.
        registry.addResourceHandler("/mbtix_storage/images/**")
                .addResourceLocations("file:///C:/mbtix_storage/images/");
    }
}