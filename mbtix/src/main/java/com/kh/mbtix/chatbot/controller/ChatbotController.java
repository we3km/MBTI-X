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

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import org.springframework.http.HttpMethod; // 추가
import org.springframework.http.HttpEntity; // 추가

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

    private final String uploadDir = "src/main/resources/static/chatbot_profiles/"; // 👈 이미지 저장 경로

	// 챗봇 방 목록 불러오기
	@GetMapping("/chatbot/rooms/{userId}")
	public ResponseEntity<List<ChatbotRoomResponse>> chatbotRoomList(
			@PathVariable long userId
			){
		List<ChatbotRoomResponse> list = chatbotService.selectChatbotList(userId);
		System.out.println(list);
		return ResponseEntity.ok().body(list);
	}
	
	
	
	// 챗봇 방 생성
	@PostMapping("/chatbot")
	public ResponseEntity<?> createRoom(@RequestBody CreateChatbotRoom room) {
	    int result = chatbotService.createChatbot(room);
	    if (result > 0) {
	        long roomId = room.getRoomId();
	        
	        // 1. FastAPI에 프로필 이미지 생성 요청
	        String fastApiImageUrl = "http://localhost:8000/generate_profile_image";
	        try {
	            ResponseEntity<Map> imageResponse = restTemplate.postForEntity(fastApiImageUrl, room, Map.class);
	            String openaiImageUrl = (String) imageResponse.getBody().get("imageUrl");

	            // 2. 이미지 URL로부터 이미지 다운로드 및 서버에 저장
	            String savedImageUrl = saveImageFromUrl(openaiImageUrl, roomId);
	            room.setBotProfileImageUrl(savedImageUrl);
	            
	            // 3. DB에 저장된 이미지 URL 업데이트
	            chatbotService.updateChatbotProfileImage(roomId, savedImageUrl);

	        } catch (Exception e) {
	            log.error("Error generating or saving profile image", e);
	            // 이미지 생성 실패 시에도 챗봇방은 생성되도록 함
	            room.setBotProfileImageUrl("/default_profile.png");
	            chatbotService.updateChatbotProfileImage(roomId, "/default_profile.png");
	        }
	        
	        // 4. FastAPI에 챗봇 초기 메시지 생성 요청
	        String fastApiUrl = "http://localhost:8000/initial_message";
	        Map<String, Object> requestBody = new HashMap<>();
	        requestBody.put("mbti", room.getBotMbti());
	        requestBody.put("botName", room.getBotName());
	        requestBody.put("gender", room.getGender());
	        requestBody.put("talkStyle", room.getTalkStyle());
	        requestBody.put("age", room.getAge());
	        requestBody.put("features", room.getFeatures());
	        
	        ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl, requestBody, Map.class);
	        String initialMessageContent = (String) response.getBody().get("message");
	
	        // 5. 받아온 메시지를 DB에 저장
	        ChatMessageSave initialMessage = new ChatMessageSave(0, room.getRoomId(), "bot", initialMessageContent);
	        chatbotService.saveMessage(initialMessage);
			
	        // 6. 생성된 방 정보와 이미지 URL을 함께 반환
	        ChatbotRoomResponse createdRoom = new ChatbotRoomResponse(
	            roomId, room.getUserId(), room.getBotMbti(), room.getBotName(),
	            null, room.getGender(), room.getTalkStyle(), room.getAge(),
	            room.getFeatures(), room.getBotProfileImageUrl()
	        );
	        
	        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom); // 👈 응답값 수정
	    } else {
	        return ResponseEntity.badRequest().build(); 
	    }
	}
	
    // 챗봇 프로필 이미지를 서버에 저장하는 메서드
    private String saveImageFromUrl(String imageUrl, long roomId) throws IOException {
        URI imageUri = URI.create(imageUrl);
        Path uploadPath = Paths.get(uploadDir);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        String fileName = "profile_" + roomId + ".png";
        Path filePath = uploadPath.resolve(fileName);
        
        try (var in = imageUri.toURL().openStream()) {
            Files.copy(in, filePath);
        }
        
        // 서버에서 접근 가능한 URL 반환
        return "/chatbot_profiles/" + fileName;
    }
	
	// 채팅방 메시지 불러오기
	@GetMapping("/chatbot/{roomId}/messages")
	public ResponseEntity<List<ChatMessageResponse>> getMessage(@PathVariable long roomId){
		List<ChatMessageResponse> list = chatbotService.getMessage(roomId);
		
		return ResponseEntity.ok().body(list);
	}
	
	// 채팅방 메시지 저장
	@PostMapping("/chatbot/{roomId}/message")
	public ResponseEntity<Long> saveMessage(
			@PathVariable Long roomId,
            @RequestBody ChatMessageSave req
			) {
		req.setRoomId(roomId);
        int result = chatbotService.saveMessage(req);
        if(result > 0) {
        	return ResponseEntity.ok().body(req.getMessageId());
        }else {
        	return ResponseEntity.badRequest().build();
        }
	}
}