package com.kh.mbtix.balgame.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kh.mbtix.balgame.mapper.BalGameMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BalScheduler {

    private final BalGameMapper mapper;

    // 매일 자정(0시)에 실행
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void resetDailyGame() {
        // 1) 기존 오늘의 게임 비활성화
        mapper.deactivateTodayGame();
        // 2) 새로운 게임 하나 활성화
        mapper.activateNewGame();

        System.out.println("[스케줄러] 자정 배치 실행: 오늘의 게임 교체 완료");
    }
}
