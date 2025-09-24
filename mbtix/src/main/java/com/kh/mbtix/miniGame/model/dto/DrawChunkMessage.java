package com.kh.mbtix.miniGame.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrawChunkMessage {
	private String id; // 전체 그림 데이터를 식별하는 고유 ID
	private int index; // 현재 조각의 순서 (0, 1, 2...)
	private int total; // 전체 조각의 개수
	private String chunk; // 잘게 쪼개진 데이터 조각 (문자열)
	private int userId;
}