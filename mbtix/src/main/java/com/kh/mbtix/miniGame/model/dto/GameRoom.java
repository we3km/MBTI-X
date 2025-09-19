package com.kh.mbtix.miniGame.model.dto;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRoom {
	private int roomId; // 방 고유 ID
	private String status; // 방 상태 (WAITING, PLAYING)

	// 참여중인 플레이어 목록
	private Map<Integer, Gamer> players = new ConcurrentHashMap<>();

	// 현재 라운드 정보
	private int currentRound;
	private int currentDrawerId; // 현재 그림 그리는 사람의 userId
	private String correctAnswer; // 현재 라운드의 정답
	private List<String> wordsForDrawer; // 출제자에게만 보여줄 단어 3개
	private int remainingTime; // 현재 타이머의 남은 시간
	private Gamer captain; // 방장 유저 객체

	// 생성자: 방이 처음 만들어질 때 초기화
	public GameRoom(int roomId) {
		this.roomId = roomId;
		this.status = "START"; // 초기 상태는 게임 시작 전
		this.currentRound = 0;
		// players는 방 생성 시 참여자 정보로 채워줍니다.
	}

	// 시간을 1초 감소시키는 편의 메소드
	public void decrementTime() {
		if (this.remainingTime > 0) {
			this.remainingTime--;
		}
	}
}
