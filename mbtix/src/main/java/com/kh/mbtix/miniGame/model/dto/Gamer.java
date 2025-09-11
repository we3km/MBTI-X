package com.kh.mbtix.miniGame.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Gamer {
	private int userId;
	private String nickname;
	private int points = 0; // 게임 내 초깃값
	private String mbtiName;
}
