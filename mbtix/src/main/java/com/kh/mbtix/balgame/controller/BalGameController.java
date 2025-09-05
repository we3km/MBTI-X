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

import com.kh.mbtix.balgame.model.dto.BalGameDtos.PastListRes;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.StatsRes;
import com.kh.mbtix.balgame.model.service.BalGameService;


	@RestController
	@RequestMapping("/api/balance")
	public class BalGameController {
	  private final BalGameService svc;
	  public BalGameController(BalGameService svc){ this.svc = svc; }

	  // (예시) 인증 필터에서 userId 주입되었다고 가정
	  private long currentUserId(){ return 1001L; }
	  
	  /**
	     * 오늘의 밸런스게임 조회 API
	     
	     * - 오늘 날짜 & IS_ACTIVE=Y 인 게임을 조회
	     * - 옵션(A/B) 리스트와 함께 내려줌
	     */

	  @GetMapping("/today")
	  
	  public ResponseEntity<?> today(){
	    var res = svc.getToday(currentUserId());
	    return res==null ? ResponseEntity.noContent().build() : ResponseEntity.ok(res);
	  }
	  
	  /**
	     * 지난 밸런스게임 목록 조회 API (페이징 지원)
	     * GET /api/balance/past?page=1&size=8
	     * - 어제 이전에 시작했거나 IS_ACTIVE=N 인 게임들을 가져옴
	     * - 목록(게임 제목, 날짜)과 페이지 정보 반환
	     */

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
	  public ResponseEntity<?> vote(@PathVariable long gameId, @RequestBody VoteReq req){
	    try {
	      svc.vote(gameId, req.option(), currentUserId());
	      return ResponseEntity.noContent().build();
	    } catch (IllegalStateException e){
	      if ("ALREADY_VOTED".equals(e.getMessage()))
	        return ResponseEntity.status(HttpStatus.CONFLICT).body("already_voted");
	      return ResponseEntity.badRequest().body(e.getMessage());
	    }
	  }
	  public record VoteReq(String option) {}
	  
	  
	  /**
	     * 특정 게임 통계 조회 API
	     * GET /api/balance/{gameId}/stats
	     * - A/B 총 투표 수와 비율
	     * - A/B 각각 MBTI별 투표 수와 비율
	     * 프런트에서 막대/파이 차트용 데이터로 사용 가능
	     */

	  @GetMapping("/{gameId}/stats")
	  public StatsRes stats(@PathVariable long gameId){
	    return svc.stats(gameId);
	  }
	}

