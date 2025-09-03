package com.kh.mbtix.chatbot.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.ChatbotRoomResponse;
import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.CreateChatbotRoom;
import com.kh.mbtix.chatbot.model.service.ChatbotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController 
public class ChatbotController {

	private final ChatbotService chatbotService;
	
	@GetMapping("/chatbot/rooms/{userId}")
	public ResponseEntity<List<ChatbotRoomResponse>> menuDetail(
			@PathVariable long userId
			){
		List<ChatbotRoomResponse> list = chatbotService.selectChatbotList(userId);
		System.out.println(list);
		
		return ResponseEntity.ok().body(list);
	}
	
	@PostMapping("/chatbot")
//	@CrossOrigin(origins="http://localhost:5173")
	public ResponseEntity<Void> createRoom(
				@RequestBody CreateChatbotRoom room
			) {
		long userId = 1; // 로그인 추가후 변경
		int result = chatbotService.createChatbot(room);
		
		if(result > 0) {
			// Post 요청의 경우 응답데이터 header에 이동할 URI 정보를 적어주는 것이 규칙
			URI location = URI.create("/chat");
			// 201 Created
			return ResponseEntity.created(location).build();
		}else {
			// 400 bad Request
			return ResponseEntity.badRequest().build(); 
		}
	}
}
