package com.kh.mbtix.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        // 단일 스레드로 동작하는 스케줄러를 생성합니다.
        // 여러 게임방의 타이머가 동시에 실행되더라도 순차적으로 처리되어 안전합니다.
        return Executors.newSingleThreadScheduledExecutor();
    }
}