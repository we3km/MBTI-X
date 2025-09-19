package com.kh.mbtix.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//또는 WebMvcConfigurer를 구현하여 정적 자원 핸들링 추가 설정
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		String uploadPath = System.getProperty("user.dir") + "/uploads/";
		registry.addResourceHandler("/uploads/**").addResourceLocations("file:" + uploadPath);
	}
}