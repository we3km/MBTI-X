package com.kh.mbtix.miniGame.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRoom {
	private int roomId; // 방 고유 ID
	private String roomName; // 방 제목
	private String creatorId; // 방 생성자 ID
	private String status; // 방 상태 (WAITING, PLAYING)
	private int playerCount; // 현재 인원
}
