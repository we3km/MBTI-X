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

public class BalGameController {
	@RestController
	@RequestMapping("/api/balance")
	public class BalController {
	  private final BalGameService svc;
	  public BalController(BalGameService svc){ this.svc = svc; }

	  // (예시) 인증 필터에서 userId 주입되었다고 가정
	  private long currentUserId(){ return 1001L; }

	  @GetMapping("/today")
	  public ResponseEntity<?> today(){
	    var res = svc.getToday(currentUserId());
	    return res==null ? ResponseEntity.noContent().build() : ResponseEntity.ok(res);
	  }

	  @GetMapping("/past")
	  public PastListRes past(@RequestParam(defaultValue="1") int page,
	                          @RequestParam(defaultValue="8") int size){
	    return svc.getPast(page,size);
	  }

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

	  @GetMapping("/{gameId}/stats")
	  public StatsRes stats(@PathVariable long gameId){
	    return svc.stats(gameId);
	  }
	}
}
