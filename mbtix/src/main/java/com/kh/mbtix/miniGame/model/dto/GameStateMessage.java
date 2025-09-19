package com.kh.mbtix.miniGame.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameStateMessage {
	// 게임 상태
	/*
	    "start" : 게임 시작 안함
        "waiting" : 게임 본격 시작, 그림 그리는 사람이 그림 고름
        "result" : 라운드 종료 후 누가 맞췄는지 나옴
        "drawing" : drawer가 그림 그리는 중
        "final" : 게임 제일 마지막 결과화면
	 * */
	private String status; // 상태 메세지
	
	@Builder.Default
	private int round = 1; // 라운드
	private int timer; // 남은 시간

	// 라운드 정보
	private Gamer drawer; // 그림 그리는 Gamer
	private List<String> words; // 묘사할 단어 배열 3가지 무작위 
	private String answer; // 각 라운드 별 정답
	private int answerLength; // 정답 길이
	
	private List<Gamer> gamers;
	private Gamer captain; // 방장
}
