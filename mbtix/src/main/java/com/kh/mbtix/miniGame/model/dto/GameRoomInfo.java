
package com.kh.mbtix.miniGame.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRoomInfo {
	// 게임방 대기 리스트에서 보여질 것
	private int roomId; // 방 id
	private String roomName; // 방 제목
	private int creatorId; // 방 생성자 ID
	private int playerCount; // 현재 인원
	private String status; // 게임을 하고 있는지 아닌지
	private String nickname; // 방 생성자 닉네임
	private String mbtiName; // 방 생성자 mbti
	private String profile; // 생성자 프로필 (경로 + 프로필 img)
}
