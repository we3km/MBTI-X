package com.kh.mbtix.mypage.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.mbtix.mypage.model.service.MyPageService;
import com.kh.mbtix.security.model.dto.AuthDto.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MyPageController {
	
	private final MyPageService service;
	
	@PutMapping("/updateNick")
	public ResponseEntity<User> updateNick(@RequestParam String newNickname, Long userId,Long point){
		try {
		  User updatedUser = service.updateNickname(userId, newNickname);
		 	  return ResponseEntity.ok(updatedUser);
    } catch (Exception e) {
        log.error("닉네임 변경 실패", e);
        return ResponseEntity.badRequest().build();

	}
	
	}
	@GetMapping("/checkPw")
    public ResponseEntity<Boolean> checkPW(@RequestParam String currentPw,@RequestParam Long userId) {
        try {
            boolean isValid = service.checkPassword(userId, currentPw);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.error("비밀번호 확인 실패", e);
            return ResponseEntity.badRequest().body(false);
        }
    }
	
	@PutMapping("/updatePw")
	public ResponseEntity<User> updatePw(@RequestParam String newPW,@RequestParam Long userId){
		try {
			User updateUser = service.updatePw(newPW,userId);
			return ResponseEntity.ok(updateUser);
		}catch (Exception e) {
			log.error("비밀번호 변경 실패",e);
			return ResponseEntity.badRequest().build();
		}
	}
}
