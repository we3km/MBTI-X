package com.kh.mbtix.chatbot.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.mbtix.chatbot.model.dao.ChatbotDao;
import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.ChatbotRoomResponse;
import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.CreateChatbotRoom;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {
	@Autowired
	private final ChatbotDao chatbotDao;

	@Override
	public int createChatbot(CreateChatbotRoom room) {
		return chatbotDao.createChatbot(room);
	}

	@Override
	public List<ChatbotRoomResponse> selectChatbotList(long userId) {
		return chatbotDao.selectChatbotList(userId);
	} 

}
