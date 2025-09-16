package com.kh.mbtix.chatbot.model.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ChatbotRoom {
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CreateChatbotRoom{
		private long roomId;
		private long userId;
		private String botMbti;
		private String botName;	
		private String gender;
		private String talkStyle;
		private int age;
		private String features;
		private String botProfileImageUrl; // 👈 이미지 URL 필드 추가
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ChatbotRoomResponse{
		private long roomId;
		private long userId;
		private String botMbti;
		private String botName;
		private Date createdAt;
		private String gender;
		private String talkStyle;
		private int age;
		private String features;
		private String botProfileImageUrl; // 👈 이미지 URL 필드 추가
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ChatbotProfileUpdate{
		private long roomId;
		private String botProfileImageUrl;
	}
	
}
