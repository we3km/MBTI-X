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

	// 게임방 대기 리스트에서 보여질 것
	private String roomName; // 방 제목
	private int creatorId; // 방 생성자 ID
	private int playerCount; // 현재 인원
	private String nickname; // 방 생성자 닉네임
	private String mbtiName; // 방 생성자 mbti
	private String profile; // 생성자 프로필 (경로 + 프로필 img)
	private int userId; // 게임방 참여중인 회원들 번호
}
