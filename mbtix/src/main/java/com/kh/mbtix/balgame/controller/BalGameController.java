package com.kh.mbtix.balgame.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.mbtix.balgame.model.dto.BalGameDtos.CreateGameReq;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.CreateGameRes;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.PastListRes;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.StatsRes;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.VoteReq;
import com.kh.mbtix.balgame.model.service.BalGameService;

import lombok.RequiredArgsConstructor;


	@RestController
	@RequestMapping("/balance")
	@RequiredArgsConstructor
	public class BalGameController {
	  private final BalGameService svc;
	  // (예시) 인증 필터에서 userId 주입되었다고 가정
	  private long currentUserId(){ return 1L; }
	  
	  // 오늘의 게임

	  @GetMapping("/today")
	  public ResponseEntity<?> today(){
	    var res = svc.getToday(currentUserId());
	    return res==null ? ResponseEntity.noContent().build() : ResponseEntity.ok(res);
	  }
	  
	// 과거 게임 목록
	  @GetMapping("/past")
	  public PastListRes past(@RequestParam(defaultValue="1") int page,
	                          @RequestParam(defaultValue="8") int size){
	    return svc.getPast(page,size);
	  }
	  
	  /**
	     * 특정 게임에 투표하기 API
	     * POST /api/balance/{gameId}/vote
	     * RequestBody: { "option": "A" }  // A 또는 B
	     * - 회원만 투표 가능 (USER_ID 필수)
	     * - 중복투표 시 409 Conflict 리턴
	     */
	  @PostMapping("/{gameId}/vote")
	  public ResponseEntity<?> vote(@PathVariable long gameId, @RequestBody VoteReq req) {
	      try {
	    	  long userId = 1L;
	          svc.vote(gameId, req.option(), currentUserId()); // req.option() == "A" or "B"
	          return ResponseEntity.ok("투표 완료");
	      } catch (IllegalStateException e) {
	          if ("ALREADY_VOTED".equals(e.getMessage())) {
	              return ResponseEntity.status(HttpStatus.CONFLICT).body("already_voted");
	          }
	          return ResponseEntity.badRequest().body(e.getMessage());
	      }
	  }

	  public record VoteReq(String option) {}
	  
	 
	// 특정 게임 통계
	    @GetMapping("/{gameId}/stats")
	    public ResponseEntity<StatsRes> stats(@PathVariable Long gameId) {
	        return ResponseEntity.ok(svc.stats(gameId));
	    }
	    
	    // 게임 생성
	    @PostMapping
	    public ResponseEntity<CreateGameRes> create(@RequestBody CreateGameReq req) {
	        var res = svc.createGame(req);
	        return ResponseEntity.ok(res);
	    }
	    
	    
	    
	    
	    
	    
	    
	    
	    
	}

