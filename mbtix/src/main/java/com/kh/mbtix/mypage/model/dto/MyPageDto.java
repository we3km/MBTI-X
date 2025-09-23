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
	public static class UserBoard{
		private Long boardId;
		private String boardTitle;
		private String nickName;
		private String createdAt;
		private Long viewCount;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UserProfileDto{
		private Long userId;
		private String nickname;
		private String email;
		private String mbtiName;
		private String profileType;
		private String profileFileName;
		private Integer point;
	}
	
	
	
}
