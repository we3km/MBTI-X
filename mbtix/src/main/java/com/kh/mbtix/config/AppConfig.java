package com.kh.mbtix.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
    	// 타이머용 스레드 스케쥴러 (여러 방있으니 병렬적으로 쓰자이)
        return Executors.newSingleThreadScheduledExecutor();
    }
}