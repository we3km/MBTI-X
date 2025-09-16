package com.kh.mbtix.chatbot.model.dao;

import java.util.List;

import com.kh.mbtix.chatbot.model.dto.ChatMessageDto.ChatMessageResponse;
import com.kh.mbtix.chatbot.model.dto.ChatMessageDto.ChatMessageSave;
import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.ChatbotRoomResponse;
import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.CreateChatbotRoom;

public interface ChatbotDao {

	int createChatbot(CreateChatbotRoom room);

	List<ChatbotRoomResponse> selectChatbotList(long userId);

	List<ChatMessageResponse> getMessage(long roomId);

	int saveMessage(ChatMessageSave req);

	String getNickName(long userId);

	void updateChatbotProfileImage(long roomId, String savedImageUrl);
	
	

}
