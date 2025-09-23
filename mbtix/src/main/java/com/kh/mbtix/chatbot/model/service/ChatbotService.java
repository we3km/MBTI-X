package com.kh.mbtix.chatbot.model.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.kh.mbtix.chatbot.model.dto.ChatMessageDto.ChatMessageResponse;
import com.kh.mbtix.chatbot.model.dto.ChatMessageDto.ChatMessageSave;
import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.ChatbotRoomResponse;
import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.CreateChatbotRoom;

public interface ChatbotService {

	int createChatbot(CreateChatbotRoom room);

	List<ChatbotRoomResponse> selectChatbotList(long userId);

	List<ChatMessageResponse> getMessage(long roomId);

	int saveMessage(ChatMessageSave req);

	String getNickName(long userId);

	void updateChatbotProfileImage(long roomId, String savedImageUrl);
	
	void updateChatbotIsQuit(long roomId);
}
