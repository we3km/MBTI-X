package com.kh.mbtix.miniGame.model.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.mbtix.miniGame.model.dto.ChatMessage;
import com.kh.mbtix.miniGame.model.dto.DrawChunkMessage;
import com.kh.mbtix.miniGame.model.dto.DrawMessage;
import com.kh.mbtix.miniGame.model.dto.GameRoom;
import com.kh.mbtix.miniGame.model.dto.GameRoomInfo;
import com.kh.mbtix.miniGame.model.dto.GameStateMessage;
import com.kh.mbtix.miniGame.model.dto.Gamer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineGameServiceImpl implements OnlineGameService {

	private List<String> allWords = new ArrayList<>();

	private final SimpMessagingTemplate messagingTemplate;
	// AppConfig에 등록한 Bean을 주입받아야 합니다.
	private final ScheduledExecutorService scheduler;
	private final MiniGameService miniGameService;

	// 게임방들 관리용 Map
	private final Map<Integer, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
	private final Map<Integer, GameRoom> gameRooms = new ConcurrentHashMap<>();

	private final Map<Integer, Map<String, Map<Integer, String>>> chunkBuffers = new ConcurrentHashMap<>();

	private final Map<Integer, Long> lastBroadcastTime = new ConcurrentHashMap<>();
	private static final long BROADCAST_INTERVAL_MS = 100; // 100ms (0.1초)

	// stopTimer 메소드
	public void stopTimer(int roomId) {
		ScheduledFuture<?> task = scheduledTasks.get(roomId);
		if (task != null && !task.isDone()) {
			task.cancel(false);
			scheduledTasks.remove(roomId);
		}
	}

	// 서비스 초기화 시점에 단어로드
	@PostConstruct
	public void init() {
		try {
			allWords = miniGameService.selectCathMindWords();
		} catch (Exception e) {
			log.error("캐치마인드 단어 로드 중 오류 발생: {}", e.getMessage(), e);
			allWords = Arrays.asList("사과", "바나나", "컴퓨터", "노트북", "의자", "책상", "호랑이", "코끼리", "자전거", "비행기");
		}
	}

	// drawer 무작위로 할당
	private Gamer selectRandomDrawer(List<Gamer> gamers) {
		if (gamers == null || gamers.isEmpty()) {
			return null;
		}
		int randomIndex = new Random().nextInt(gamers.size());
		return gamers.get(randomIndex);
	}

	// 무작위 단어 3개 선택
	private List<String> getRandomWords(int count) {
		List<String> shuffledList = new ArrayList<>(allWords);
		Collections.shuffle(shuffledList);

		int wordsToPick = Math.min(count, shuffledList.size());

		return shuffledList.subList(0, wordsToPick);
	}

	// 게임 루프
	public void startTimerForState(int roomId, String status) {
		stopTimer(roomId);
		GameRoom room = gameRooms.get(roomId);
		if (room == null)
			return;

		int durationSeconds;
		Runnable onTimerEnd;

		switch (status.toLowerCase()) {
		// 단어 선택 기다리기
		case "waiting":
			durationSeconds = 5;
			onTimerEnd = () -> {
				GameRoom currentRoom = gameRooms.get(roomId);
				if (currentRoom == null)
					return;
				// 시간이 끝났는데도 정답이 아직 안 정해졌는지 확인
				if (currentRoom.getCorrectAnswer() == null) {
					log.info("{}번 방 단어 선택 시간 초과. 서버가 무작위로 선택합니다.", roomId);
					List<String> words = currentRoom.getWordsForDrawer();
					if (words != null && !words.isEmpty()) {
						// 3개의 단어 중 하나를 무작위로 뽑아 정답으로 설정
						String randomAnswer = words.get(new Random().nextInt(words.size()));
						currentRoom.setCorrectAnswer(randomAnswer);
						log.info("{}번 방의 정답이 '{}'(으)로 확정되었습니다. (서버 자동 선택)", roomId, randomAnswer);
					}
				}
				// 정답이 확정되었으니 '그림 그리기' 단계로 이동
				startDrawingPhase(roomId);
			};
			break;
		case "drawing":
			durationSeconds = 30;
			onTimerEnd = () -> showRoundResult(roomId);
			break;
		case "result":
			durationSeconds = 5;
			onTimerEnd = () -> startNextRoundOrEndGame(roomId);
			break;
		case "final":
			durationSeconds = 5;
			onTimerEnd = () -> resetGameToLobby(roomId);
			break;
		default:
			return;
		}

		room.setRemainingTime(durationSeconds);

		Runnable timerTask = () -> {
			room.decrementTime();
			GameStateMessage timeMessage = GameStateMessage.builder().timer(room.getRemainingTime()).build();
			messagingTemplate.convertAndSend("/sub/game/" + roomId + "/timer", timeMessage);

			if (room.getRemainingTime() <= 0) {
				onTimerEnd.run();
			}
		};

		// 1초 딜레이 후, 1초 간격으로 timerTask를 실행하라고 예약
		ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(timerTask, 1, 1, TimeUnit.SECONDS);
		// 예약된 작업의 '리모컨'을 나중에 중지할 수 있도록 Map에 저장
		scheduledTasks.put(roomId, task);
	}

	// ================= 게임 단계 처리 메서드 =================
	// 그림 그리는 단계
	private void startDrawingPhase(int roomId) {
		GameRoom room = gameRooms.get(roomId);
		if (room == null)
			return;
		if (room.getCorrectAnswer() == null) {
			room.setCorrectAnswer(room.getWordsForDrawer().get(0));
			log.info("이번 라운드 정답 : {}", room.getWordsForDrawer().get(0));
		}
		room.setStatus("drawing");
		startTimerForState(roomId, "drawing");

		GameStateMessage message = GameStateMessage.builder().status("drawing").drawer(getCurrentDrawerFromRoom(room))
				.answerLength(room.getCorrectAnswer().length()).answer(room.getCorrectAnswer())
				.round(room.getCurrentRound()).gamers(new ArrayList<>(room.getPlayers().values())).build();
		messagingTemplate.convertAndSend("/sub/game/" + roomId + "/state", message);
	}

	// 각 라운드 별 결과값
	private void showRoundResult(int roomId) {
		GameRoom room = gameRooms.get(roomId);
		if (room == null)
			return;
		room.setStatus("result");
		startTimerForState(roomId, "result");

		GameStateMessage message = GameStateMessage.builder().status("result").answer(room.getCorrectAnswer())
				.gamers(new ArrayList<>(room.getPlayers().values())).round(room.getCurrentRound()).build();
		messagingTemplate.convertAndSend("/sub/game/" + roomId + "/state", message);
	}

	// 다음 라운드로 넘어갈지 최종화면으로 넘어갈지
	private void startNextRoundOrEndGame(int roomId) {
		GameRoom room = gameRooms.get(roomId);
		if (room == null)
			return;
		int maxRounds = room.getPlayers().size() * 2;

		if (room.getCurrentRound() >= maxRounds) {

			for (Gamer gamer : room.getPlayers().values()) {

				int userId = gamer.getUserId();
				int score = gamer.getPoints();

				Map<String, Object> point = new HashMap<>();
				point.put("userId", userId);
				point.put("score", score);
				point.put("gameCode", 3);

				miniGameService.insertPoint(point);

				log.info("플레이어 ID: {}, 최종 점수: {}", userId, score);
			}

			room.setStatus("final");
			startTimerForState(roomId, "final");
			GameStateMessage message = GameStateMessage.builder().status("final")
					.gamers(new ArrayList<>(room.getPlayers().values())).build();
			messagingTemplate.convertAndSend("/sub/game/" + roomId + "/state", message);
		} else {
			room.setCurrentRound(room.getCurrentRound() + 1);
			// 무작위 단어, 그림 그릴 사람
			Gamer nextDrawer = selectRandomDrawer(new ArrayList<>(room.getPlayers().values()));
			List<String> nextWords = getRandomWords(3);

			room.setStatus("waiting");
			room.setCurrentDrawerId(nextDrawer.getUserId());
			room.setWordsForDrawer(nextWords);
			room.setCorrectAnswer(null);

			log.info("{}가 묘사할 무작위 단어 {}", nextDrawer, nextWords);
			startTimerForState(roomId, "waiting");

			GameStateMessage message = GameStateMessage.builder().status("waiting").drawer(nextDrawer)
					.round(room.getCurrentRound()).gamers(new ArrayList<>(room.getPlayers().values())).words(nextWords)
					.build();
			messagingTemplate.convertAndSend("/sub/game/" + roomId + "/state", message);
		}
	}

	// 최종 결과 이후 start화면으로
	private void resetGameToLobby(int roomId) {
		GameRoom room = gameRooms.get(roomId);
		if (room == null)
			return;
		stopTimer(roomId);
		room.getPlayers().values().forEach(gamer -> gamer.setPoints(0));
		room.setCurrentRound(1);
		room.setStatus("start");

		Map<String, Object> map = new HashMap<>();

		map.put("roomId", roomId);
		map.put("status", "N");

		miniGameService.setGameState(map);

		GameStateMessage message = GameStateMessage.builder().status("start")
				.gamers(new ArrayList<>(room.getPlayers().values())).build();
		messagingTemplate.convertAndSend("/sub/game/" + roomId + "/state", message);
	}

	private Gamer getCurrentDrawerFromRoom(GameRoom room) {
		if (room == null || room.getPlayers() == null)
			return null;
		return room.getPlayers().get(room.getCurrentDrawerId());
	}

	// ================= 방 메모리 데이터 관련 =================
	@Override
	public void prepareRoom(int roomId, int userId) {
		List<Gamer> gamersFromDb = miniGameService.selectGamers(roomId);
		GameRoomInfo gameRoomInfo = miniGameService.selectGameRoomInfo(roomId);

		Gamer joiningGamer = gamersFromDb.stream().filter(g -> g.getUserId() == userId).findFirst().orElse(null);

		log.info("현재 게임방 정보 : {}", gameRoomInfo);

		int captainId = gameRoomInfo.getCreatorId();
		Gamer captain = gamersFromDb.stream().filter(g -> g.getUserId() == captainId).findFirst().orElse(null);
		log.info("방장 정보 : {}", captain);

		if (gamersFromDb == null || gamersFromDb.isEmpty()) {
			gameRooms.remove(roomId); // 방 번호 비었으니 삭제
			return;
		}

		// 메모리에서 방을 찾거나, 없으면 새로 만듭니다.
		GameRoom room = gameRooms.get(roomId);
		if (room == null) {
			log.info("{}번 방을 새로 메모리에 올립니다.", roomId);
			room = new GameRoom(roomId);
			room.setStatus("start");
			gameRooms.put(roomId, room);
		}

		Map<String, String> joinSystemMessage = Map.of("message", joiningGamer.getNickname() + "님이 방을 입장하였습니다.");
		messagingTemplate.convertAndSend("/sub/chat/" + roomId, joinSystemMessage);

		// DB에서 가져온 최신 정보로 메모리의 플레이어 목록을 '덮어씁니다'.
		Map<Integer, Gamer> playersMap = gamersFromDb.stream()
				.collect(Collectors.toMap(Gamer::getUserId, gamer -> gamer));
		room.setPlayers(playersMap);
		room.setCaptain(captain);

		log.info("✅ {}번 방 메모리 적재/갱신 완료. 참여자 수: {}", roomId, room.getPlayers().size());

		// 4. 현재 방의 최신 상태를 모든 클라이언트에게 전송합니다.
		GameStateMessage initialStateMessage = GameStateMessage.builder().status(room.getStatus()) // 현재 방의 상태를 그대로 유지
				.gamers(new ArrayList<>(room.getPlayers().values())).captain(captain).build();

		messagingTemplate.convertAndSend("/sub/game/" + roomId + "/state", initialStateMessage);
	}

	// 방 나가기
	@Transactional
	@Override
	public void handleLeaveRoom(int roomId, int userId) {
		GameRoom room = gameRooms.get(roomId);
		if (room == null) {
			log.warn("메모리에 {}번 방이 없어 나가기 처리를 스킵합니다.", roomId);
			return;
		}

		// 나가는 플레이어 정보
		Gamer leavingGamer = room.getPlayers().get(userId);
		if (leavingGamer == null) {
			log.warn("{}번 방에 {}번 유저가 없어 나가기 처리를 스킵합니다.", roomId, userId);
			return;
		}

		chunkBuffers.remove(userId);
		log.info("[메모리 정리] {}번 유저의 chunkBuffer를 삭제했습니다.", userId);

		// 나가는 사람이 방장이었는지, 그림을 그리던 출제자였는지
		Gamer captain = room.getCaptain();
		log.info("captain , leavingGamer , {} , {}", captain, leavingGamer);
		boolean wasCaptain = captain != null && captain.getUserId() == leavingGamer.getUserId();
		boolean wasDrawerInDrawingPhase = leavingGamer.getUserId() == room.getCurrentDrawerId()
				&& "drawing".equalsIgnoreCase(room.getStatus());

		room.getPlayers().remove(userId);
		log.info("{}번 유저가 {}번 방의 메모리에서 제거되었습니다.", userId, roomId);

		if (room.getPlayers().isEmpty()) {
			log.info("{}번 방이 비어있어 방을 제거하고 타이머를 중지합니다.", roomId);
			gameRooms.remove(roomId);
			scheduledTasks.remove(roomId);
			lastBroadcastTime.remove(roomId);
			stopTimer(roomId);
			return; // 모든 로직 종료
		}

		// 방에 혼자만 남겨졌을 때
		if (room.getPlayers().size() == 1) {
			log.info("{}번 방에 한 명만 남아 게임을 대기 상태로 변경합니다.", roomId);
			stopTimer(roomId); // 현재 라운드 타이머 즉시 중지

			room.setStatus("start");
			room.setCurrentRound(1);
			room.setCorrectAnswer(null);
			room.setWordsForDrawer(null);

			Gamer lastPlayer = new ArrayList<>(room.getPlayers().values()).get(0);
			lastPlayer.setPoints(0); // 점수도 초기화
			room.setCaptain(lastPlayer);

			Map<String, Object> captainInfo = new HashMap<>();
			captainInfo.put("userId", lastPlayer.getUserId());
			captainInfo.put("roomId", roomId);
			miniGameService.changeCaptain(captainInfo);

			// 방 상태 바꾸는 로직
			Map<String, Object> roomState = new HashMap<>();
			roomState.put("roomId", roomId);
			roomState.put("status", "N");
			miniGameService.setGameState(roomState);

			Map<String, String> aloneMessage = Map.of("message", "플레이어가 한명만 남아 게임 초기화면으로 이동합니다.");
			messagingTemplate.convertAndSend("/sub/chat/" + roomId, aloneMessage);
			
			GameStateMessage resetMessage = GameStateMessage.builder().status("start")
					.gamers(new ArrayList<>(room.getPlayers().values())).captain(room.getCaptain()).build();
			messagingTemplate.convertAndSend("/sub/game/" + roomId + "/state", resetMessage);
			
			return;
		}

		// 1. 그림을 그리던 출제자가 나갔을 경우 (가장 먼저 처리)
		if (wasDrawerInDrawingPhase) {
			log.info("출제자({})가 나가 현재 라운드를 종료하고 다음 라운드를 시작합니다.", leavingGamer.getNickname());
			stopTimer(roomId); // 현재 라운드 타이머 즉시 중지
			Map<String, String> systemMessage = Map.of("message", "출제자가 나가서 현재 라운드가 종료됩니다.");
			messagingTemplate.convertAndSend("/sub/chat/" + roomId, systemMessage);
			startNextRoundOrEndGame(roomId);
		} else {
			// 2. 나간 사람이 방장이었을 경우
			if (wasCaptain) {
				Gamer newCaptain = new ArrayList<>(room.getPlayers().values()).get(0);
				room.setCaptain(newCaptain);
				log.info("방장이 나가 새로운 방장이({})님으로 변경되었습니다.", newCaptain.getNickname());

				Map<String, Object> captainInfo = new HashMap<>();
				captainInfo.put("userId", newCaptain.getUserId());
				captainInfo.put("roomId", roomId);
				miniGameService.changeCaptain(captainInfo);

				// 통합된 알림 메시지 전송
				Map<String, String> captainChangeMessage = Map.of("message",
						"방장 " + leavingGamer.getNickname() + "님이 나가서 새로운 방장은 " + newCaptain.getNickname() + "님입니다.");
				messagingTemplate.convertAndSend("/sub/chat/" + roomId, captainChangeMessage);
			} else {
				// 일반 유저가 나갔을 경우
				Map<String, String> leaveSystemMessage = Map.of("message", leavingGamer.getNickname() + "님이 방을 나갔습니다.");
				messagingTemplate.convertAndSend("/sub/chat/" + roomId, leaveSystemMessage);
			}
			// 3. 단순히 업데이트된 게임 상태만 모두에게 브로드캐스팅
			GameStateMessage leaveMessage = GameStateMessage.builder().status(room.getStatus()) // 현재 상태는 유지
					.gamers(new ArrayList<>(room.getPlayers().values())) // 최신 플레이어 목록
					.captain(room.getCaptain()) // 방장이 바뀌었을 수 있으니 최신 정보 전송
					.build();
			messagingTemplate.convertAndSend("/sub/game/" + roomId + "/state", leaveMessage);
		}
		log.info("방 상태 : {}", room);
	}

	@Override
	public void startGame(int roomId) {
		// 해당방에 접속 중인 게이머들 리스틀 가져옴
		List<Gamer> gamers = miniGameService.selectGamers(roomId);
		log.info("접속중인 플레이어들: {}" + gamers);

		GameRoom room = gameRooms.get(roomId);
		if (room == null) {
			System.out.println(roomId + "번 방이 존재하지 않습니다.");
		}

		Collections.shuffle(gamers);
		Gamer drawer = gamers.get(0);

		List<String> words = getRandomWords(3);

		room.setStatus("waiting");
		room.setCurrentDrawerId(drawer.getUserId());
		room.setWordsForDrawer(words); // 제시어 저장
		room.setCurrentRound(1);

		startTimerForState(roomId, "waiting");

		GameStateMessage publicMessage = GameStateMessage.builder().status(room.getStatus()).gamers(gamers)
				.drawer(drawer).round(room.getCurrentRound()).words(words).build();

		messagingTemplate.convertAndSend("/sub/game/" + roomId + "/state", publicMessage);

		log.info("{} 번 방 게임 시작! 첫 출제자는 {}", roomId, drawer.getNickname());
	}

	// drawer가 단어 선택했을 경우
	@Override
	public void selectWord(int roomId, String answer) {
		GameRoom room = gameRooms.get(roomId);
		if (room == null || !"waiting".equalsIgnoreCase(room.getStatus())) {
			// 'waiting' 상태가 아니면 요청 무시
			return;
		}
		room.setCorrectAnswer(answer);
		log.info("{}번 방의 정답이 '{}'(으)로 확정되었습니다. (사용자 선택)", roomId, answer);
		startDrawingPhase(roomId);
	}

	// 실시간 채팅 및 정답 확인
	@Override
	public void checkAnswerAndBroadcast(int roomId, ChatMessage request) {
		GameRoom room = gameRooms.get(roomId);
		Gamer sender = room.getPlayers().get(request.getUserId());

		// "drawing" 상태가 아니거나, 출제자가 채팅을 친 경우는 정답 확인 없이 일반 채팅으로 처리
		if (!"drawing".equalsIgnoreCase(room.getStatus()) || sender.getUserId() == room.getCurrentDrawerId()) {
			Map<String, String> chatMessage = Map.of("user", sender.getNickname(), "message", request.getMessage());
			messagingTemplate.convertAndSend("/sub/chat/" + roomId, chatMessage);
			return;
		}

		// 정답을 맞혔을 경우
		if (request.getMessage().equals(room.getCorrectAnswer())) {
			log.info("정답! | 방: {}, 유저: {}", roomId, sender.getNickname());

			sender.setPoints(sender.getPoints() + 10);
			Gamer drawer = room.getPlayers().get(room.getCurrentDrawerId());
			if (drawer != null) {
				drawer.setPoints(drawer.getPoints() + 5);
			}

			Map<String, String> correctMessage = Map.of("message", "\"" + sender.getNickname() + "\" 님이 정답을 맞혔습니다!");
			messagingTemplate.convertAndSend("/sub/chat/" + roomId, correctMessage);

			// 즉시 라운드 결과 화면으로 전환
			showRoundResult(roomId);

		} else { // 정답이 아닐 경우 (일반 채팅)
			Map<String, String> chatMessage = Map.of("user", sender.getNickname(), "message", request.getMessage());
			messagingTemplate.convertAndSend("/sub/chat/" + roomId, chatMessage);
		}
	}

	// 그림판 그리기
	@Override
	public void drawAndBroadCast(int roomId, DrawMessage message) {
		GameRoom room = gameRooms.get(roomId);
		// 그림 그리기 상태일 때만 그림 정보를 전송하도록 방어 코드 추가

		log.info("2. [서버] 그림 정보 수신 성공! 다시 모든 클라이언트로 전송합니다. ");
		if (room != null && "drawing".equalsIgnoreCase(room.getStatus())) {
			messagingTemplate.convertAndSend("/sub/draw/" + roomId, message);
		}
	}

	// ===================== 그림판 메세지 데이터 처리 =====================
	@Override
	public void handleDrawChunk(int roomId, DrawChunkMessage message) {
//		log.info("[RECEIVE CHUNK] roomId: {}, chunkId: {}, index: {}/{}", roomId, message.getId(),
//				message.getIndex() + 1, message.getTotal());

		GameRoom room = gameRooms.get(roomId);
		if (room == null || !"drawing".equalsIgnoreCase(room.getStatus()))
			return;

		int userId = message.getUserId();
		Map<String, Map<Integer, String>> userBuffer = chunkBuffers.computeIfAbsent(userId, k -> new HashMap<>());
		Map<Integer, String> chunks = userBuffer.computeIfAbsent(message.getId(), k -> new HashMap<>());

		chunks.put(message.getIndex(), message.getChunk());

		// 1. 모든 조각이 도착했는지 확인합니다.
		if (chunks.size() == message.getTotal()) {

			// ✅ 2. [가장 먼저] 도착한 모든 조각을 합쳐서 fullDataString을 만듭니다.
			StringBuilder fullDataString = new StringBuilder();
			for (int i = 0; i < message.getTotal(); i++) {
				fullDataString.append(chunks.get(i));
			}

			// 3. 마지막으로 전송한 시간을 확인합니다.
			long currentTime = System.currentTimeMillis();
			long lastTime = lastBroadcastTime.getOrDefault(roomId, 0L);

			// ✅ 4. [그 다음에] 전송 간격(100ms)이 지났는지 확인합니다.
			if (currentTime - lastTime > BROADCAST_INTERVAL_MS) {
//				log.info("[BROADCASTING] assembledSize: {}", fullDataString.length());

				// ✅ 5. 위에서 만든 fullDataString을 사용해 데이터를 전송합니다.
				messagingTemplate.convertAndSend("/sub/draw/" + roomId, fullDataString.toString());
				lastBroadcastTime.put(roomId, currentTime);
			} else {
				log.trace("[THROTTLED] Broadcasting skipped for roomId: {}", roomId);
			}

			// 6. 조립이 끝난 데이터는 메모리에서 즉시 삭제합니다.
			userBuffer.remove(message.getId());
		}
	}

	@Override
	public void updateAndNotifyRoomInfo(GameRoomInfo updatedInfo) {
		
		
	}
}