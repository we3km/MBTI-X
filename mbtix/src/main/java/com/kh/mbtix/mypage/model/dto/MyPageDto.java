package com.kh.mbtix.mypage.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MyPageDto {
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class GameScore{
		private Long quizScore;
		private Long findDiffScore;
		private Long reactionScore;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class MyBoard{
		private Long boardId;
		private String boardTitle;
		private String nickName;
		private String createdAt;
		private Long viewCount;
	}
}
