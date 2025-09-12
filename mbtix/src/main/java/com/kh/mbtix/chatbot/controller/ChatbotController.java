package com.kh.mbtix.chatbot.controller;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.kh.mbtix.chatbot.model.dto.ChatMessageDto.ChatMessageResponse;
import com.kh.mbtix.chatbot.model.dto.ChatMessageDto.ChatMessageSave;
import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.ChatbotRoomResponse;
import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.CreateChatbotRoom;
import com.kh.mbtix.chatbot.model.service.ChatbotService;
import com.kh.mbtix.security.model.provider.JWTProvider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController 
public class ChatbotController {

	private final ChatbotService chatbotService;
	private final JWTProvider jwtTokenProvider;
    private final RestTemplate restTemplate;

	
	//
	@GetMapping("/chatbot/rooms/{userId}")
	public ResponseEntity<List<ChatbotRoomResponse>> chatbotRoomList(
			@PathVariable long userId
			){
		List<ChatbotRoomResponse> list = chatbotService.selectChatbotList(userId);
		System.out.println(list);
		
		return ResponseEntity.ok().body(list);
	}
	
	@PostMapping("/chatbot")
//	@CrossOrigin(origins="http://localhost:5173")
	public ResponseEntity<Long> createRoom(
				@RequestBody CreateChatbotRoom room
			) {
		int result = chatbotService.createChatbot(room);
		
		if(result > 0) {
            // 2. FastAPI에 LLM 초기 메시지 생성 요청
            String fastapiUrl = "http://localhost:8000/initial_message";
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("mbti", room.getBotMbti());
            requestBody.put("botName", room.getBotName());
            String nickname = chatbotService.getNickName(room.getUserId());
            requestBody.put("nickname", nickname);

            requestBody.put("gender", room.getGender());
            requestBody.put("talkStyle", room.getTalkStyle());
            requestBody.put( "age", Integer.toString( room.getAge() ) );
            requestBody.put("features", room.getFeatures());
            // LLM이 생성한 자기소개 메시지를 받아옴
            ResponseEntity<Map> response = restTemplate.postForEntity(fastapiUrl, requestBody, Map.class);
            String initialMessageContent = (String) response.getBody().get("message");

            // 3. 받아온 메시지를 DB에 저장 (새로운 서비스 메소드 추가 필요)
            ChatMessageSave initialMessage = new ChatMessageSave(0, room.getRoomId(), "bot", initialMessageContent);
            chatbotService.saveMessage(initialMessage);
			
			// Post 요청의 경우 응답데이터 header에 이동할 URI 정보를 적어주는 것이 규칙
			URI location = URI.create("/chat");
			// 201 Created
			return ResponseEntity.created(location).body(room.getRoomId());
		}else {
			// 400 bad Request
			return ResponseEntity.badRequest().build(); 
		}
	}
	
	
	// 채팅방 메시지 불러오기
	@GetMapping("/chatbot/{roomId}/messages")
	public ResponseEntity<List<ChatMessageResponse>> getMessage(@PathVariable long roomId){
		List<ChatMessageResponse> list = chatbotService.getMessage(roomId);
		
		return ResponseEntity.ok().body(list);
	}
	
	

	// 채팅방 메시지지 저장
	@PostMapping("/chatbot/{roomId}/message")
//	@CrossOrigin(origins="http://localhost:5173")
	public ResponseEntity<Long> saveMessage(
			@PathVariable Long roomId,
            @RequestBody ChatMessageSave req
			) {
		req.setRoomId(roomId);
        int result = chatbotService.saveMessage(req);


        if (result > 0) {
            URI location = URI.create("/chat/" + roomId);
            return ResponseEntity.created(location).build();
        } else {
            return ResponseEntity.badRequest().build();
        }
	}
	
	
}
