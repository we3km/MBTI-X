package com.kh.mbtix.balComment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kh.mbtix.balComment.model.dto.BalCommentDto.BalComment;
import com.kh.mbtix.balComment.service.BalCommentService;

/**
 * 밸런스게임 댓글 컨트롤러
 * - 댓글 조회, 작성, 삭제 담당
 */
@RestController
@RequestMapping("/balance/comments")
class BalCommentController {

    private final BalCommentService svc;

    public BalCommentController(BalCommentService svc) {
        this.svc = svc;
    }

    /**
     * 특정 게임(balId)의 댓글 목록 조회
     */
    @GetMapping("/{balId}")
    public List<BalComment> list(@PathVariable long balId) {
        return svc.findByBalId(balId);
    }

    /**
     * 댓글 작성
     * (실서비스에서는 SecurityContext에서 userId 추출해야 함)
     */
    @PostMapping
    public void write(@RequestBody BalComment dto) {
        svc.write(dto);
    }

    /**
     * 댓글 삭제 (본인만 가능)
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> delete(
            @PathVariable long commentId,
            @RequestParam long userId // 현재는 요청에서 userId를 받도록 처리
    ) {
        boolean success = svc.delete(commentId, userId);
        if (success) {
            return ResponseEntity.ok("삭제 성공");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("삭제 권한이 없습니다.");
        }
    }
}
