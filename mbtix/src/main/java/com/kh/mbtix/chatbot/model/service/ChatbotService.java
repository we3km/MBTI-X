package com.kh.mbtix.chatbot.model.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.ChatbotRoomResponse;
import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.CreateChatbotRoom;

public interface ChatbotService {

	int createChatbot(CreateChatbotRoom room);

	List<ChatbotRoomResponse> selectChatbotList(long userId);

}
