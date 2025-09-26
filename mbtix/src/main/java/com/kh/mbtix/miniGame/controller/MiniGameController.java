package com.kh.mbtix.miniGame.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kh.mbtix.miniGame.model.dto.GameRoom;
import com.kh.mbtix.miniGame.model.dto.GameRoomInfo;
import com.kh.mbtix.miniGame.model.dto.Gamer;
import com.kh.mbtix.miniGame.model.dto.Quiz;
import com.kh.mbtix.miniGame.model.service.MiniGameService;
import com.kh.mbtix.miniGame.model.service.OnlineGameService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController // @ResponseBody + @Controlle
//@CrossOrigin(origins = { "http://localhost:5173", "http://192.168.10.230:5173", "http://52.65.147.249/" })
public class MiniGameController {
	private final MiniGameService miniGameService;
	private final OnlineGameService onlineGameService;

	@GetMapping("/speedquiz")
	public ResponseEntity<List<Quiz>> quizList() {
		List<Quiz> list = miniGameService.selectQuiz();
		return ResponseEntity.ok(list);
	}

	@PostMapping("/point")
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
	public Map<Integer, List<Map<String, Object>>> getRank() {
		List<Map<String, Object>> allRanks = miniGameService.getRank();

		// gameCode별 MBTI 포인트 기준 내림차순
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
	public List<Map<String, Object>> getUserMBTI() {
		return miniGameService.getUserMBTI();
	}

	// 메인페이지 오늘의 밸런스 게임 제목 가져오기
	@GetMapping("/getQuizTitle")
	public String getQuizTitle() {
		return miniGameService.getQuizTitle();
	}

	// 온라인 게임 방 만들기
	@PostMapping("/createGameRoom")
	public ResponseEntity<Integer> createGameRoom(@RequestBody Map<String, Object> data) {

		String roomName = ((String) data.get("title"));
		int userId = ((Number) data.get("userId")).intValue();
		int maxCount = ((Number) data.get("maxPlayers")).intValue();

		Map<String, Object> map = new HashMap<>();
		map.put("roomName", roomName);
		map.put("userId", userId);
		map.put("maxCount", maxCount);

		// 서비스 호출해서 DB에 저장
		int roomId = miniGameService.createGameRoom(map);
		if (roomId > 0) {
			log.info("{}번호 회원이 {}방 생성", userId, roomId);
			onlineGameService.prepareRoom(roomId, userId);
		} else
			log.info("방 생성 실패");

		return ResponseEntity.ok(roomId);
	}

	// ==================== 게임방 리스트 들어가고 나서부터 ====================

	// 게임방 정보 가져오기
	@GetMapping("/selectGameRoomInfo")
	public GameRoomInfo selectGameRoomInfo(int roomId) {
		return miniGameService.selectGameRoomInfo(roomId);
	}

	// 게임방 리스트 불러오기
	@GetMapping("/selectGameRoomList")
	public List<GameRoom> selectGameRoomList() {
		return miniGameService.selectGameRoomList();
	}

	// 게임방 내 게이머들
	@GetMapping("/selectGamers")
	public List<Gamer> selectGamers(int roomId) {
		List<Gamer> gr = miniGameService.selectGamers(roomId);
		log.debug("게임 이용자들 : {}", gr);
		return gr;
	}

	@PostMapping("/leaveRoom")
	public Map<String, Object> leaveRoom(@RequestBody Map<String, Integer> payload) {
		int roomId = payload.get("roomId");
		int userId = payload.get("userId");
		int isKickedOut = payload.get("isKickedOut");
		// isKickedOut = 1이면 강퇴, 0이면 자진 퇴소!!

		Map<String, Object> map = new HashMap<>();
		map.put("roomId", roomId);
		map.put("userId", userId);

		miniGameService.leaveRoom(map);

		map.put("isKickedOut", isKickedOut);
		List<Gamer> remainingGamers = miniGameService.selectGamers(roomId);
		if (remainingGamers == null || remainingGamers.isEmpty()) {
			log.info("{}번 방이 비었으므로 DB에서 삭제합니다.", roomId);
			miniGameService.deleteRoom(roomId);
		}
		log.info("나가는 정보 : {}", map);
		onlineGameService.handleLeaveRoom(roomId, userId, isKickedOut);

		return Map.of("status", "success");
	}

	@PostMapping("/joinGameRoom")
	public Map<String, Object> joinGameRoom(@RequestBody Map<String, Integer> payload) {
		int roomId = payload.get("roomId");
		int userId = payload.get("userId");

		Map<String, Object> map = new HashMap<>();
		map.put("roomId", roomId);
		map.put("userId", userId);

		// 게임 시작중인지 확인
		String status = miniGameService.getGameRoomStatus(map);
		if (status.equals("Y")) {
			return Map.of("status", "fail ");
		} else {
			miniGameService.joinGameRoom(map);
			onlineGameService.prepareRoom(roomId, userId);
			return Map.of("status", "success");
		}
	}

	public List<String> selectCathMindWords() {
		return miniGameService.selectCathMindWords();
	}

	@Transactional
	@PostMapping("/changeRoomInfo")
	public void changeRoomInfo(@RequestBody Map<String, Object> payload) {
		String roomName = (String) payload.get("roomName");
		int roomId = (int) payload.get("roomId");
		int maxCount = (int) payload.get("maxCount");

		Map<String, Object> map = new HashMap<>();
		map.put("roomName", roomName);
		map.put("maxCount", maxCount);
		map.put("roomId", roomId);

		miniGameService.changeRoomInfo(map);

		// 넣은 정보 다시 조회
		GameRoomInfo updatedInfo = miniGameService.selectGameRoomInfo(roomId);

		onlineGameService.updateAndNotifyRoomInfo(updatedInfo);
	}
}
