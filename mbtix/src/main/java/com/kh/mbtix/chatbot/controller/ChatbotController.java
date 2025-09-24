// ChatbotController.java (수정된 전체 코드)

package com.kh.mbtix.chatbot.controller;

import java.io.FileOutputStream;
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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.kh.mbtix.chatbot.model.dto.ChatMessageDto.ChatMessageResponse;
import com.kh.mbtix.chatbot.model.dto.ChatMessageDto.ChatMessageSave;
import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.ChatbotRoomResponse;
import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.CreateChatbotRoom;
import com.kh.mbtix.chatbot.model.service.ChatbotService;
import com.kh.mbtix.security.model.provider.JWTProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController 
public class ChatbotController {

	private final ChatbotService chatbotService;
	private final JWTProvider jwtTokenProvider;
    private final RestTemplate restTemplate;

    // WebConfig에 설정된 경로와 동일한 논리로 경로 설정
    private final String uploadDir = System.getProperty("user.dir") + "/uploads/chatbot";

    // 챗봇 방 목록 불러오기
	@GetMapping("/chatbot/rooms/{userId}")
	public ResponseEntity<List<ChatbotRoomResponse>> chatbotRoomList(
			@PathVariable long userId
			){
		List<ChatbotRoomResponse> list = chatbotService.selectChatbotList(userId);
		return ResponseEntity.ok().body(list);
	}

    /**
     * 1단계: 프로필 이미지를 미리 생성하여 프론트엔드로 반환하는 엔드포인트
     * - 이미지 생성 요청을 파이썬 서버로 전달하고, 받은 긴 URL을 그대로 반환합니다.
     */
    @PostMapping("/chatbot/generate-image")
    public ResponseEntity<Map<String, String>> generateImage(@RequestBody CreateChatbotRoom req) {
        try {
            String fastApiImageUrl = "http://localhost:8000/generate-image";
            // req 객체에 personality와 appearance가 포함되어 FastAPI로 전달됩니다.
            ResponseEntity<Map> imageResponse = restTemplate.postForEntity(fastApiImageUrl, req, Map.class);
            // FastAPI에서 반환하는 Base64 데이터 URL을 그대로 프론트엔드에 전달
            String openaiImageUrl = (String) imageResponse.getBody().get("imageUrl");
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("imageUrl", openaiImageUrl);
            return ResponseEntity.ok().body(responseMap);

        } catch (Exception e) {
            log.error("이미지 생성 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
	
    /**
     * 2단계: 최종 챗봇방 생성
     * - 프론트엔드에서 전달받은 긴 이미지 URL을 서버에 저장하고, 짧은 경로를 DB에 저장합니다.
     */
	@PostMapping("/chatbot")
	public ResponseEntity<?> createRoom(@RequestBody CreateChatbotRoom room) {
		log.info("Received request to create a chatbot: {}", room);
	    
		String savedImageUrl = "/uploads/chatbot_profiles/default_profile.png"; // 기본 이미지 경로
		
		// 이미지 URL이 Base64 데이터인지 확인 후 파일로 저장
	    if (room.getBotProfileImageUrl() != null && room.getBotProfileImageUrl().startsWith("data:image")) {
	        try {
	            // Base64 데이터를 파일로 저장하고 저장된 경로를 반환받음
	            savedImageUrl = saveImageFromBase64(room.getBotProfileImageUrl());
	            // DTO의 URL을 서버에 저장된 파일 경로로 업데이트
	            room.setBotProfileImageUrl(savedImageUrl);
	        } catch (IOException e) {
	            log.error("Failed to save Base64 image.", e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	        }
	    }
	    
	    // 1. 챗봇방을 먼저 생성하고 roomId를 받아옵니다.
		int result = chatbotService.createChatbot(room);
	    if (result <= 0) {
	        return ResponseEntity.badRequest().build();
	    }
	    
	    long roomId = room.getRoomId();
	    
	    // 2. DB에 짧은 이미지 URL(로컬 경로)을 업데이트합니다.
	    chatbotService.updateChatbotProfileImage(roomId, savedImageUrl);

	    // 3. FastAPI에 챗봇 초기 메시지 생성 요청
	    String fastApiUrl = "http://localhost:8000/initial_message";
	    Map<String, Object> requestBody = new HashMap<>();
	    requestBody.put("mbti", room.getBotMbti());
	    requestBody.put("botName", room.getBotName());
	    requestBody.put("gender", room.getGender());
	    requestBody.put("talkStyle", room.getTalkStyle());
	    requestBody.put("age", room.getAge());
        // personality와 appearance를 각각 전달
	    requestBody.put("personality", room.getPersonality());
	    requestBody.put("appearance", room.getAppearance());
	    
	    ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl, requestBody, Map.class);
	    String initialMessageContent = (String) response.getBody().get("message");

	    // 4. 받아온 메시지를 DB에 저장
	    ChatMessageSave initialMessage = new ChatMessageSave(0, roomId, "bot", initialMessageContent);
	    chatbotService.saveMessage(initialMessage);
		
	    // 5. 생성된 방 정보와 이미지 URL을 함께 반환
	    ChatbotRoomResponse createdRoom = new ChatbotRoomResponse(
	        roomId, room.getUserId(), room.getBotMbti(), room.getBotName(),
	        null, room.getGender(), room.getTalkStyle(), room.getAge(),
	        room.getPersonality(), room.getAppearance(), savedImageUrl, "Y"
	    );
	    
	    return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
	}
	
	// Base64 이미지를 서버에 저장하는 메소드
	private String saveImageFromBase64(String base64Image) throws IOException {
	    // "data:image/png;base64," 부분을 제거
	    String base64Data = base64Image.substring(base64Image.indexOf(",") + 1);
	    byte[] imageBytes = Base64.getDecoder().decode(base64Data);

	    Path uploadPath = Paths.get(uploadDir);
	    if (!Files.exists(uploadPath)) {
	        Files.createDirectories(uploadPath);
	    }
	    
	    String uniqueId = UUID.randomUUID().toString();
	    String fileName = "profile_" + uniqueId + ".png";
	    Path filePath = uploadPath.resolve(fileName);
	    
	    try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
	        fos.write(imageBytes);
	    }
	    
	    // 프론트에서 접근할 수 있는 상대 경로를 반환
	    return "/uploads/chatbot/" + fileName;
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
	
	/**
	 * 챗봇방을 삭제하는 대신 status 속성을 'Y'로 업데이트합니다.
	 */
	@PatchMapping("/chatbot/{roomId}/quit")
	public ResponseEntity<Void> quitChatbotRoom(@PathVariable long roomId){
		try {
			chatbotService.updateChatbotIsQuit(roomId);
			return ResponseEntity.ok().build();
		}catch(Exception e) {
			log.error("챗봇방 비활성화 실패", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}