package com.kh.mbtix.admin;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kh.mbtix.admin.model.service.AdminService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private AdminService adminService;

	// 스피드 퀴즈, 캐치마인드 단어 데이터 넣기
	@PostMapping("/insertGameData")
    public ResponseEntity<Void> insertGameData(@RequestBody Map<String, Object> data) {
        adminService.insertGameData(data);
        
        log.info("받은 데이터 : {}", data);
        return ResponseEntity.ok().build();
    }
}