// ChatbotController.java (수정된 전체 코드)

package com.kh.mbtix.chatbot.controller;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.kh.mbtix.chatbot.model.dto.ChatMessageDto.ChatMessageResponse;
import com.kh.mbtix.chatbot.model.dto.ChatMessageDto.ChatMessageSave;
import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.ChatbotRoomResponse;
import com.kh.mbtix.chatbot.model.dto.ChatbotRoom.CreateChatbotRoom; // 이 DTO에 personality와 appearance 필드가 있어야 합니다.
import com.kh.mbtix.chatbot.model.service.ChatbotService;
import com.kh.mbtix.security.model.provider.JWTProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController 
@CrossOrigin(origins = "http://localhost:5173")
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
            String fastApiImageUrl = "http://localhost:8000/generate_profile_image";
            // req 객체에 personality와 appearance가 포함되어 FastAPI로 전달됩니다.
            ResponseEntity<Map> imageResponse = restTemplate.postForEntity(fastApiImageUrl, req, Map.class);
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
        // DB에 저장하기 위해 personality와 appearance를 features로 조합
		//String combinedFeatures = "성격: " + room.getPersonality() + "\n외모: " + room.getAppearance();
        //room.setFeatures(combinedFeatures);

	    // 1. 챗봇방을 먼저 생성하고 roomId를 받아옵니다.
	    int result = chatbotService.createChatbot(room);
	    if (result <= 0) {
	        return ResponseEntity.badRequest().build();
	    }
	    
	    long roomId = room.getRoomId();
	    String savedImageUrl;
	    
	    // 2. 프론트에서 받은 긴 이미지 URL을 서버에 저장하고, 저장된 짧은 경로를 반환받습니다.
	    try {
            if (room.getBotProfileImageUrl() != null && !room.getBotProfileImageUrl().isEmpty()) {
                // saveImageFromUrl에서 웹 경로를 반환하도록 변경
                savedImageUrl = saveImageFromUrl(room.getBotProfileImageUrl(), roomId);
                System.out.println(savedImageUrl);
            } else {
                savedImageUrl = "/chatbot_profiles/default_profile.png";
            }
	    } catch (Exception e) {
	        log.error("Error saving profile image", e);
	        savedImageUrl = "/chatbot_profiles/default_profile.png";
	    }
	    
	    // 3. DB에는 짧은 이미지 URL(로컬 경로)을 업데이트합니다.
	    chatbotService.updateChatbotProfileImage(roomId, savedImageUrl);

	    // 4. FastAPI에 챗봇 초기 메시지 생성 요청
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

	    // 5. 받아온 메시지를 DB에 저장
	    ChatMessageSave initialMessage = new ChatMessageSave(0, roomId, "bot", initialMessageContent);
	    chatbotService.saveMessage(initialMessage);
		
	    // 6. 생성된 방 정보와 이미지 URL을 함께 반환
	    ChatbotRoomResponse createdRoom = new ChatbotRoomResponse(
	        roomId, room.getUserId(), room.getBotMbti(), room.getBotName(),
	        null, room.getGender(), room.getTalkStyle(), room.getAge(),
	        room.getPersonality(), room.getAppearance(), savedImageUrl
	    );
	    
	    return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
	}
	
    // 챗봇 프로필 이미지를 서버에 저장하는 메서드
    private String saveImageFromUrl(String imageUrl, long roomId) throws IOException {
        URI imageUri = URI.create(imageUrl);
        Path uploadPath = Paths.get(uploadDir);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        String uniqueId = UUID.randomUUID().toString();
        String fileName = "profile_" + roomId + "_" + uniqueId + ".png";
        
        Path filePath = uploadPath.resolve(fileName);
        
        try (var in = imageUri.toURL().openStream()) {
            Files.copy(in, filePath);
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
}