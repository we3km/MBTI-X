package com.kh.mbtix.test.model.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MbtiModelDto {
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Question {
		 private Long id;
		 private String question;
		 private String aType;
		 private String bType;
		
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Answer{
		private Long questionId;
	    private String choice; // "A" or "B"
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class MbtiTestRequest {
	    private Long userId;
	    private List<Answer> answers;
	}
	
	public record MbtiRatioRes(String mbtiName, double ratio) {}
	
	public record MbtiDetailRes(
		    String type,
		    Map<String, Map<String,Integer>> ratios
		) {}


}
