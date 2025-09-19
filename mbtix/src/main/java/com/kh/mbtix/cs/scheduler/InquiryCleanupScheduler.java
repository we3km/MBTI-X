package com.kh.mbtix.cs.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kh.mbtix.cs.model.service.InquiryService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class InquiryCleanupScheduler {

	@Autowired
	private InquiryService inquiryService;
	
	// 새벽 4시에 실행
	@Scheduled(cron = "0 0 4 * * *")
	public void cleanupOldInquiries() {
		log.info("오래된 1:1 문의 데이터 영구 삭제 작업을 시작합니다...");
		try {
			inquiryService.permanentlyDeleteOldInquiries();
		} catch (Exception e) {
			log.error("오래된 1:1 문의 데이터 삭제 작업 중 오류 발생", e);
		}
	}
	
}
