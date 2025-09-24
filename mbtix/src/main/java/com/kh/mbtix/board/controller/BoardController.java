package com.kh.mbtix.board.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.mbtix.admin.model.service.AdminService;
import com.kh.mbtix.admin.model.vo.Report;
import com.kh.mbtix.board.model.service.BoardService;
import com.kh.mbtix.board.model.vo.Board;
import com.kh.mbtix.board.model.vo.Board.BoardLike;
import com.kh.mbtix.board.model.vo.Board.BoardRequest;
import com.kh.mbtix.board.model.vo.BoardComment;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/board")
public class BoardController {

	private final BoardService boardService;
	private final AdminService adminService;

    @Autowired
    public BoardController(BoardService boardService, AdminService adminService) {
        this.boardService = boardService;
        this.adminService = adminService;
    }

	@GetMapping
	public ResponseEntity<List<Board>> selectBoardList(
			@RequestParam Map<String, Object> param
			){
		log.debug("param : {}",param);
		List<Board> list = boardService.selectBoardList(param);
		
		return ResponseEntity.ok().body(list);
	}
	
	@GetMapping("/{boardId}")
	public ResponseEntity<Board> selectBoard(@PathVariable int boardId){
		Board b = boardService.selectBoard(boardId);
		
		if(b == null) {
			return ResponseEntity.notFound().build();
		}
		boardService.incrementView(boardId);
		
		return ResponseEntity.ok().body(b);
	}
	
	@PostMapping
	public ResponseEntity<Board> insertBoard(@ModelAttribute BoardRequest request){ // [수정] 반환 타입을 ResponseEntity<Board>로 변경
	    Object principal =  SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	    if (!(principal instanceof Long)) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    }
	    Long userId = (Long) principal;

	    Board b = Board.builder()
	            .title(request.getTitle())
	            .content(request.getContent())
	            .categoryId(request.getCategoryId())	            
	            .mbtiName(request.getMbtiName())
	            .userId(userId)
	            .boardMbti(request.getBoardMbti())
	            .build();
	    
	    int result = boardService.insertBoard(b , request.getImages());
	    
	    if(result > 0) {
	        return ResponseEntity.status(HttpStatus.CREATED).body(b); 
	    }else {
	        return ResponseEntity.badRequest().build();
	    }
	}
	
    @PostMapping("/report")
    public ResponseEntity<String> submitReport(
            @RequestBody Report report,
            @AuthenticationPrincipal Long userId) {
        
        report.setUserId(userId.intValue());
        
        int result = adminService.createReport(report);

        if (result > 0) {
            return ResponseEntity.ok("신고가 성공적으로 접수되었습니다.");
        } else {
            return ResponseEntity.status(500).body("신고 접수 중 오류가 발생했습니다.");
        }
    }
	
	@GetMapping("/{boardId}/comments")
    public List<BoardComment> getComments(@PathVariable int boardId) {
        return boardService.getComments(boardId);
    }
	
    @PostMapping("/comments")
    public BoardComment addComment(@RequestBody BoardComment comment) {
    	Object userId =  SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	if(userId instanceof Long) {
    		comment.setUserId((Long)userId);
    	}
    	
        boardService.insertComment(comment);
        return comment;
    }
    
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(BoardComment comment , @PathVariable int commentId) {
    	Object userId =  SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    	if(userId instanceof Long) {
    		comment.setUserId((Long)userId);
    	}    	
    	comment.setCommentId(commentId);
        
    	int result = boardService.deleteComment(comment);
    	
    	if(result == 0) {
    		return ResponseEntity.badRequest().build();
    	}
    	return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{boardId}")
    public ResponseEntity<String> deleteBoard(
            @PathVariable int boardId,
            @AuthenticationPrincipal Long userId
    ) {
        boolean deleted = boardService.deleteBoard(boardId, userId);
        if (deleted) {
            return ResponseEntity.ok("삭제 성공");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("권한 없음");
        }
    }
	
    @PostMapping("/boardLike/{boardId}")
    public ResponseEntity<Void> insertLike(@PathVariable int boardId , @RequestBody BoardLike like, @AuthenticationPrincipal Long userId){
		like.setBoardId(boardId);
		like.setUserId(userId);
		
		int result = boardService.insertLike(like);
		
		if(result > 0) {
			return ResponseEntity.noContent().build();
		}else {
			return ResponseEntity.badRequest().build();
		}
	}
}