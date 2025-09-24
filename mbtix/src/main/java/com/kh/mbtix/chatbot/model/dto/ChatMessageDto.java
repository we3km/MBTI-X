package com.kh.mbtix.chatbot.model.dto;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ChatMessageDto {

	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ChatMessageResponse{
		private long messageId;
		private long roomId;
		private String sender;
		private String content;
		private Date createdAt;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ChatMessageSave{
		private long messageId;
		private long roomId;
		private String sender;
		private String content;
	}
}
