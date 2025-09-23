package com.kh.mbtix.balComment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.mbtix.balComment.model.dto.BalCommentDto.BalComment;
import com.kh.mbtix.balComment.service.BalCommentService;

@RestController
@RequestMapping("/balance/comments")
class BalCommentController {
	
	
	
	 private final BalCommentService svc;

	    public BalCommentController(BalCommentService svc) {
	        this.svc = svc;
	    }

	    @GetMapping("/{balId}")
	    public List<BalComment> list(@PathVariable long balId){
	        return svc.findByBalId(balId);  // ✅ 매개변수명도 balId
	    }

	    @PostMapping
	    public void write(@RequestBody BalComment dto){
	        // 실서비스에서는 SecurityContext에서 userId 꺼내서 넣기
	        svc.write(dto);
	    }
	    
	 // 댓글 삭제 (본인만)
	    @DeleteMapping("/{commentId}")
	    public ResponseEntity<String> delete(
	            @PathVariable long commentId,
	            @RequestParam long userId  // 로그인한 사용자 ID (현재는 요청으로 받기)
	    ){
	        boolean success = svc.delete(commentId, userId);
	        if (success) {
	            return ResponseEntity.ok("삭제 성공");
	        } else {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN)
	                                 .body("삭제 권한이 없습니다.");
	        }
	    }
	}
