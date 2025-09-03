package com.kh.mbtix.miniGame.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kh.mbtix.miniGame.model.dto.Quiz;
import com.kh.mbtix.miniGame.model.service.MiniGameService;
import com.kh.mbtix.security.model.dto.AuthDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 점수 컨트롤
@Slf4j
@RequiredArgsConstructor
@RestController // @ResponseBody + @Controller
public class MiniGameController {
	private final MiniGameService miniGameService;

	@GetMapping("/speedquiz")
	@CrossOrigin(origins = "http://localhost:5173")
	public ResponseEntity<List<Quiz>> quizList() {
		List<Quiz> list = miniGameService.selectQuiz();
		return ResponseEntity.ok(list);
	}

	@PostMapping("/point")
	@CrossOrigin(origins = "http://localhost:5173")
	public Map<String, Object> insertPoint(@RequestBody Map<String, Object> point) {
		int gameCode = ((Number) point.get("GAME_CODE")).intValue();
		int score = ((Number) point.get("SCORE")).intValue();
		int userId = ((Number) point.get("USER_ID")).intValue();

		// =========== 사용자 ID 넣는 로직 필요!! ===========
		Map<String, Object> map = new HashMap<>();
		map.put("gameCode", gameCode);
		map.put("score", score);
		map.put("userId", userId);

		miniGameService.insertPoint(map);

		return Map.of("status", "success"); // JSON 반환
	}

	// 가져올 랭크 (미니 게임 메인페이지, 게임랭크 페이지에서 재탕하자잇)
	@GetMapping("/rank")
	@CrossOrigin(origins = "http://localhost:5173")
	public Map<Integer, List<Map<String, Object>>> getRank() {
		List<Map<String, Object>> allRanks = miniGameService.getRank();

		// gameCode별 상위 3개 MBTI
		return allRanks.stream()
				.collect(
						Collectors
								.groupingBy(r -> ((Number) r.get("GAME_CODE")).intValue(),
										Collectors.collectingAndThen(Collectors.toList(),
												list -> list.stream()
														.sorted((a, b) -> ((Number) b.get("TOTAL_SCORE")).intValue()
																- ((Number) a.get("TOTAL_SCORE")).intValue())
														.toList())));
	}

	@GetMapping("/getUserMBTI")
	@CrossOrigin(origins = "http://localhost:5173")
	public List<Map<String, Object>> getUserMBTI() {
		return miniGameService.getUserMBTI();
	}

	// 메인페이지 오늘의 밸런스 게임 제목 가져오기
	@GetMapping("/getQuizTitle")
	@CrossOrigin(origins = "http://localhost:5173")
	public ResponseEntity<String> getQuizTitle() {
		return ResponseEntity.ok(miniGameService.getQuizTitle());
	}
}
