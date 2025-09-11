package com.kh.mbtix.miniGame.model.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.kh.mbtix.miniGame.model.dto.GameRoom;
import com.kh.mbtix.miniGame.model.dto.GameStateMessage;
import com.kh.mbtix.miniGame.model.dto.Gamer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OnlineGameServiceImpl implements OnlineGameService {
	private final SimpMessagingTemplate messagingTemplate; // WebSocket 메시지 전송용

	// AppConfig에 등록한 Bean을 주입받아야 합니다.
	private final ScheduledExecutorService scheduler;
	private final Map<Integer, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

	// 게임방들 관리용 Map
	private final Map<Integer, GameRoom> gameRooms = new ConcurrentHashMap<>();

	private final MiniGameService miniGameService;

	List<String> allWords = Arrays.asList("사과", "바나나", "컴퓨터", "노트북", "의자", "책상", "호랑이"); // 예시 단어 목록

	/**
	 * 특정 상태에 맞는 타이머를 시작하는 메소드 (이전에 만들었던 코드)
	 */
	public void startTimerForState(int roomId, String status) {
		// 이전에 완성했던 타이머 시작/관리 코드를 여기에 그대로 붙여넣습니다.
		stopTimer(roomId); // 기존 타이머 중지

		if (gameRooms == null) {
			System.out.println("게임룸 비었다.");
		}
		GameRoom room = gameRooms.get(roomId);
		if (room == null)
			return;

		int durationSeconds;
		Runnable onTimerEnd;

		switch (status) {
		case "waiting":
			durationSeconds = 5;
			onTimerEnd = () -> {
				/* 단어 선택 시간 초과 로직 */ };
			break;
		case "drawing":
			durationSeconds = 5;
			onTimerEnd = () -> {
				/* 그리기 시간 초과 로직 */ };
			break;
		case "result":
			durationSeconds = 5;
			onTimerEnd = () -> {
				/* 그리기 시간 초과 로직 */ };
			break;
		case "final":
			durationSeconds = 5;
			onTimerEnd = () -> {
				/* 최종 결과 */
			};
			break;
		default:
			return;
		}

		room.setRemainingTime(durationSeconds);
		// ... scheduleAtFixedRate 등 타이머 실행 로직 전체 ...

		Runnable timerTask = () -> {
			room.decrementTime();
			// 남은 시간을 클라이언트에 전송하는 로직 (messagingTemplate 사용)
			// ...

			if (room.getRemainingTime() <= 0) {
				onTimerEnd.run(); // 타이머가 끝나면 정의된 액션 실행
			}
		};

		// ✅ 2. 주입받은 scheduler를 여기서 사용합니다.
		// 1초 딜레이 후, 1초 간격으로 timerTask를 실행하라고 예약을 겁니다.
		ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(timerTask, 1, 1, TimeUnit.SECONDS);

		// 예약된 작업의 '리모컨'을 나중에 중지할 수 있도록 Map에 저장
		scheduledTasks.put(roomId, task);
	}

	// stopTimer 메소드도 필요합니다.
	public void stopTimer(int roomId) {
		// ... 타이머 중지 로직 ...
	}

	
	
	
	
	
	
	
	
	
	@Override
	public void startGame(int roomId) {
		// 해당방에 접속 중인 게이머들 리스틀 가져옴
		List<Gamer> gamers = miniGameService.selectGamers(roomId);
		System.out.println("접속중인 플레이어들: " + gamers);

		// 1. Map에서 현재 게임방의 상태 객체를 가져옵니다.
		// (방이 처음 생성될 때 gameRooms.put(roomId, new GameRoom(roomId)); 코드가 실행되었다고 가정)
		GameRoom room = gameRooms.get(roomId);
		System.out.println("가져오는 방: " + room);
		if (room == null) {
			System.out.println(roomId + "번 방이 존재하지 않습니다.");
		}

		// 2. 출제자 선정 및 제시어 준비
		Collections.shuffle(gamers);

		Gamer drawer = gamers.get(0);
		System.out.println("drawer: " + drawer);
		
		// 무작위 단어 3개
		Collections.shuffle(allWords);
		List<String> words = allWords.subList(0, Math.min(3, allWords.size()));

		// 3. 가져온 GameRoom 객체의 상태를 직접 변경합니다. (이제 메소드가 존재!)
		room.setStatus("waiting");
		room.setCurrentDrawerId(drawer.getUserId());
		room.setWordsForDrawer(words); // 제시어 저장
		room.setCurrentRound(1);

		// 4. 단어 선택 타이머 시작! (10초)
		startTimerForState(roomId, "waiting");

		// 5. 모든 클라이언트에게 새로운 상태 전파
		GameStateMessage publicMessage = GameStateMessage.builder()
				.status(room.getStatus())
				.gamers(gamers) // 전체 플레이어 목록
				.drawer(drawer)
				.round(room.getCurrentRound()).build();

		messagingTemplate.convertAndSend("/sub/game/" + roomId + "/state", publicMessage);
		// 출제자에게만 제시어 정보

		System.out.println(roomId + "번 방 게임 시작! 첫 출제자는 " + drawer.getNickname());
	}

	
	@Override
	public void waitingGame(int roomId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resultGame(int roomId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawingGame(int roomId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finalGame(int roomId) {
		// TODO Auto-generated method stub
		
	}
}