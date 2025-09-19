package com.kh.mbtix.balgame.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.kh.mbtix.balgame.model.dto.BalGameDtos.TodayListRes;
import com.kh.mbtix.balgame.model.service.BalGameService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/balance")
@RequiredArgsConstructor
public class BalGameController {
    private final BalGameService svc;

    private long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        throw new IllegalStateException("로그인 정보가 없습니다.");
    }
    
    @GetMapping("/me")
    public Map<String, Object> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        return Map.of(
            "userId", auth.getName(),
            "roles", auth.getAuthorities().stream()
                         .map(Object::toString)
                         .toList()
        );
    }
    
//    @GetMapping("/me")
//    public Map<String, Object> me() {
//        // ✅ 테스트: userId=44, ROLE_ADMIN으로 강제 로그인
//        var auth = new UsernamePasswordAuthenticationToken(
//            44L,                         // principal (userId)
//            null,                        // credentials
//            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")) // 권한
//        );
//        SecurityContextHolder.getContext().setAuthentication(auth);
//
//        var current = SecurityContextHolder.getContext().getAuthentication();
//        return Map.of(
//            "userId", current.getPrincipal(),
//            "roles", current.getAuthorities().stream()
//                            .map(Object::toString)
//                            .toList()
//        );
//    }

    
    

    @GetMapping("/today")
    public TodayListRes today(@RequestParam(defaultValue = "1") int page) {
        return svc.getTodayPaged(currentUserId(), page, 1);
    }

    @GetMapping("/past")
    public PastListRes past(
            @RequestParam String date,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "1") int size
    ) {
        return svc.getPast(date, page, size);
    }

    @GetMapping("/past/dates")
    public List<String> pastDates() {
        return svc.getPastDates();
    }

    @GetMapping("/past/by-date")
    public List<PastListRes.PastCard> pastByDate(@RequestParam String date) {
        return svc.getPastGamesByDate(date);
    }

    @PostMapping("/{gameId}/vote")
    public ResponseEntity<?> vote(@PathVariable long gameId, @RequestBody VoteReq req) {
        try {
            svc.vote(gameId, req.option(), currentUserId());
            return ResponseEntity.ok("투표 완료");
        } catch (IllegalStateException e) {
            if ("ALREADY_VOTED".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("already_voted");
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public record VoteReq(String option) {}

    @GetMapping("/{gameId}/stats")
    public ResponseEntity<StatsRes> stats(@PathVariable Long gameId) {
        return ResponseEntity.ok(svc.stats(gameId));
    }

    @PostMapping
    public ResponseEntity<CreateGameRes> create(@RequestBody CreateGameReq req) {
        return ResponseEntity.ok(svc.createGame(req));
    }
}

