package com.kh.mbtix.miniGame.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.kh.mbtix.miniGame.model.dto.DrawMessage;
import com.kh.mbtix.miniGame.model.dto.GameStateMessage;
import com.kh.mbtix.miniGame.model.service.OnlineGameService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Controller
public class OnlineController {

	private final OnlineGameService onlineGameservice;
	private final SimpMessagingTemplate messagingTemplate;

	// ================= Status 관리 =================
	// 게임 시작
	@MessageMapping("/game/{roomId}/start")
	public void startGame(@DestinationVariable int roomId) {
		onlineGameservice.startGame(roomId);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// ---------------- 그림판 ----------------
	@MessageMapping("/draw/{roomId}")
	@SendTo("/sub/draw/{roomId}")
	public DrawMessage handleDraw(@DestinationVariable int roomId, DrawMessage message) {
		// 필요하면 검증, 로그 등 추가
		System.out.println("Room " + roomId + " Draw: " + message);
		System.out.println("Room " + roomId + " Draw Path: " + message.getPath());
		return message; // 같은 room 구독자에게 전달
	}

	// ---------------- 채팅 ----------------
//    @MessageMapping("/chat/{roomId}")
//    @SendTo("/sub/chat/{roomId}")	
//    public ChatMessage handleChat(@DestinationVariable int roomId, ChatMessage message) {
//        // 필요하면 필터링, 로그, 정답체크 등 가능
//        System.out.println("Room " + roomId + " Chat: " + message.getUser() + " - " + message.getMessage());
//        return message; // 같은 room 구독자에게 전달
//    }

	@MessageMapping("/game/{roomId}/status")
	public void updateStatus(@DestinationVariable Long roomId, GameStateMessage gameStatusMessage) {

		Map<String, Object> payload = new HashMap<>();
		payload.put("status", gameStatusMessage.getStatus());

		if ("drawing".equals(gameStatusMessage.getStatus())) {
			payload.put("answerLength", gameStatusMessage.getAnswerLength());
		} else if ("result".equals(gameStatusMessage.getStatus()) || "final".equals(gameStatusMessage.getStatus())) {
			payload.put("answer", gameStatusMessage.getAnswer());
		}

		messagingTemplate.convertAndSend("/sub/game/" + roomId + "/status", payload);
	}
}