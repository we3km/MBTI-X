package com.kh.mbtix.board.controller;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.javassist.expr.Instanceof;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.mbtix.board.model.service.BoardService;
import com.kh.mbtix.board.model.vo.Board;
import com.kh.mbtix.board.model.vo.Board.BoardRequest;
import com.kh.mbtix.board.model.vo.BoardComment;
import com.kh.mbtix.board.model.vo.Report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/board")
@CrossOrigin(
	    origins = "http://localhost:5173",   
	    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE},
	    allowedHeaders = {"Content-Type", "Authorization"} // 허용할 헤더
	)
public class BoardController {

	private final BoardService service;
	
	/** 
	 * 게시글목록 불러오기
	 *  - 페이징 기능 추가시 수정 필요
	 *  
	 * param : 사용자의 mbtiId가 담겨있는 파라미터
	 * */
	@GetMapping
	public ResponseEntity<List<Board>> selectBoardList(
			@RequestParam Map<String, Object> param
			){
		List<Board> list = service.selectBoardList(param);
		
		return ResponseEntity.ok().body(list); // 
	}
	/** 
	 * 게시글 상세보기 불러오기
	 * 1번게시글
	 * /api/board/1 
	 * */
	@GetMapping("/{boardId}")
	public ResponseEntity<Board> selectBoard(@PathVariable int boardId){
		Board b = service.selectBoard(boardId);
		
		if(b == null) {
			// 404응답상태 반환
			return ResponseEntity.notFound().build();
		}
		service.incrementView(boardId);
		
		return ResponseEntity.ok().body(b);
	}
	/** 
	 * 게시글 등록
	 *  - 유저번호는 로그인기능 추가후 수정 필요.
	 * */
	@PostMapping
	public ResponseEntity<Void> insertBoard(@ModelAttribute BoardRequest request){
		
		Board b = Board.builder()
	            .title(request.getTitle())
	            .content(request.getContent())
	            .categoryId(request.getCategoryId())
	            .mbtiName(request.getMbtiName())
	            .build();
		log.debug("board : {}", b);
		int result = service.insertBoard(b , request.getImages());
		
		// 등록 성공
		if(result > 0) {
			return ResponseEntity.noContent().build(); //응답상태 204(요청은 성공, body에는 데이터 없는 상태)
		}else {
			return ResponseEntity.badRequest().build(); // 응답상태 400
		}
	}
	
	@PostMapping("/report") // /board/report
	public ResponseEntity<Void> insertReport(@RequestBody Report r){
		int result = service.insertReport(r);
		
		// 등록 성공
		if(result > 0) {
			return ResponseEntity.noContent().build(); //응답상태 204(요청은 성공, body에는 데이터 없는 상태)
		}else {
			return ResponseEntity.badRequest().build(); // 응답상태 400
		}
	}
	
	@GetMapping("/{boardId}/comments")
    public List<BoardComment> getComments(@PathVariable int boardId) {
        return service.getComments(boardId);
    }
	
	/**
	 * 
	 * USER_ID, CONTENT, BOARD_ID
	 *  - USER_ID -> 로그인한 사용자 ID
	 *  - CONTENT, BOARD_ID -> 사용자가 작성한 댓글 내용과, 댓글이 작성된 게시글 번호 
	 *  */
    @PostMapping("/comments")
    public BoardComment addComment(@RequestBody BoardComment comment) {
    	Object userId =  SecurityContextHolder.getContext().getAuthentication().getPrincipal(); // 사용자 userId
    	if(userId.getClass().equals(Long.class)) {
    		comment.setUserId((Long)userId);
    	}
    	
        service.insertComment(comment);
        return comment;
    }
	
	
	
//	@GetMapping("/{boardId}")
//	public ResponseEntity<Board> ListBoard(@PathVariable int boardId) {
//	    service.incrementView(boardId);
//
//	    Board b = service.ListBoard(boardId);
//	    if (b == null) {
//	        return ResponseEntity.notFound().build();
//	    }
//	    return ResponseEntity.ok(b);
//	}
	
	
	
	
	
	
	
	
	
	
}
