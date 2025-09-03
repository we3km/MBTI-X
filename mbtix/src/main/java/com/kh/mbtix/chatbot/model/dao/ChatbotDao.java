package com.kh.mbtix.chatbot.model.dao;

import java.util.List;

import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.ChatbotRoomResponse;
import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.CreateChatbotRoom;

public interface ChatbotDao {

	int createChatbot(CreateChatbotRoom room);

	List<ChatbotRoomResponse> selectChatbotList(long userId);

}
