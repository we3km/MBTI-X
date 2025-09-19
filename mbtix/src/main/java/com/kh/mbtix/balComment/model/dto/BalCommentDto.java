package com.kh.mbtix.balComment.model.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class BalCommentDto {
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class BalComment{
		Long commentId;
	    Long balId;
	    Long userId;
	    String userName;
	    String mbti;
	    String content;
	    String createAt;
		
		
	}

}
