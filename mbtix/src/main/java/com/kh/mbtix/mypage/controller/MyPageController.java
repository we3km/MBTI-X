package com.kh.mbtix.mypage.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.mbtix.mypage.model.dto.MyPageDto.GameScore;
import com.kh.mbtix.mypage.model.dto.MyPageDto.UserBoard;
import com.kh.mbtix.mypage.model.dto.MyPageDto.UserProfileDto;
import com.kh.mbtix.mypage.model.service.MyPageService;
import com.kh.mbtix.mypage.model.service.ProfileFileService;
import com.kh.mbtix.security.model.dto.AuthDto.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {
	
	private final MyPageService service;
	private final ProfileFileService fileService;
	
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
	
	    @PutMapping("/updateProfileImg")
	    public ResponseEntity<User> updateProfileImg(
	            @RequestParam Long userId,
	            @RequestPart("file") MultipartFile file) {

	        // 1. 파일 저장 (UUID 파일명으로)
	        String savedFileName = fileService.saveProfile(file);

	        // 2. DB 업데이트 (포인트 차감 + 프로필 파일명 변경)
	        User updatedUser = service.updateProfileImage(userId, savedFileName);

	        // 3. 응답 반환
	        return ResponseEntity.ok(updatedUser);
	        
	        
	    }
	    @GetMapping("/profile/images/{fileName}")
	    public ResponseEntity<Resource> getProfileImage(@PathVariable String fileName) {
	        try {
	        	Path filePath = Paths.get(System.getProperty("user.dir"), "uploads", "profile", fileName);
	            Resource resource = new UrlResource(filePath.toUri());

	            if (!resource.exists()) {
	                return ResponseEntity.notFound().build();
	            }

	            String contentType = Files.probeContentType(filePath);
	            if (contentType == null) {
	                contentType = "application/octet-stream";
	            }

	            return ResponseEntity.ok()
	                    .contentType(MediaType.parseMediaType(contentType))
	                    .body(resource);

	        } catch (Exception e) {
	            e.printStackTrace(); // 로그 남기기
	            return ResponseEntity.internalServerError().build();
	        }
	    }
	    @GetMapping("/score/{userId}")
	    public ResponseEntity<GameScore> getScore(@PathVariable Long userId) {
	        GameScore score = service.getScore(userId);
	        return ResponseEntity.ok(score);
	    }
	    
	    @GetMapping("/myBoard/{userId}")
	    public ResponseEntity<List<UserBoard>> getBoard(@PathVariable Long userId){
	    	List<UserBoard> boards = service.getBoardList(userId);
	    	return ResponseEntity.ok(boards);
	    }
	    
	    @PutMapping("/deductMbtiPoint")
	    public ResponseEntity<?> requestMbtiRetest(@RequestParam Long userId) {
	        Map<String, Object> response = new HashMap<>();
	        Integer Point = service.deductMbtiPoint(userId);

	        if (Point != null) {
	            response.put("success", true);
	            response.put("point", Point);
	            return ResponseEntity.ok(response);
	        } else {
	            response.put("success", false);
	            response.put("message", "포인트가 부족합니다.");
	            return ResponseEntity.badRequest().body(response);
	        }
	}
	    
	    @GetMapping("/otherUserProfile")
	    public ResponseEntity<UserProfileDto> getOtherUserProfile(@RequestParam Long userId) {
	        UserProfileDto profile = service.findUserProfile(userId);
	        return ResponseEntity.ok(profile);
	    }

	    /**
	     * 상대방 게임 점수 조회
	     */
	    @GetMapping("/otherUserScores")
	    public ResponseEntity<GameScore> getOtherUserScores(@RequestParam Long userId) {
	    	GameScore scores = service.findUserScores(userId);
	        return ResponseEntity.ok(scores);
	    }

	    /**
	     * 상대방 게시글 목록 조회
	     */
	    @GetMapping("/otherUserBoards")
	    public ResponseEntity<List<UserBoard>> getOtherUserBoards(@RequestParam Long userId) {
	        List<UserBoard> boards = service.findUserBoards(userId);
	        return ResponseEntity.ok(boards);
	    }
	    
}
