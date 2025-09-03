package com.kh.mbtix.chatbot.model.dao;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.ChatbotRoomResponse;
import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.CreateChatbotRoom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ChatbotDaoImpl implements ChatbotDao {
	@Autowired
	private SqlSessionTemplate session;
	
	@Override
	public int createChatbot(CreateChatbotRoom room) {
		return session.insert("chatbot.createChatbot", room);
	}

	@Override
	public List<ChatbotRoomResponse> selectChatbotList(long userId) {
		return session.selectList("chatbot.selectChatbotList", userId);
	}

}
