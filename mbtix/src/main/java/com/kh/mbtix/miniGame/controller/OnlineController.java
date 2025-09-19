package com.kh.mbtix.miniGame.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.kh.mbtix.miniGame.model.dto.ChatMessage;
import com.kh.mbtix.miniGame.model.dto.DrawChunkMessage;
import com.kh.mbtix.miniGame.model.dto.GameStateMessage;
import com.kh.mbtix.miniGame.model.service.MiniGameService;
import com.kh.mbtix.miniGame.model.service.OnlineGameService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Controller
public class OnlineController {

	private final OnlineGameService onlineGameservice;
	private final SimpMessagingTemplate messagingTemplate;
	private final MiniGameService miniGameService;

	// ================= Status 관리 =================
	// 게임 시작
	@MessageMapping("/game/{roomId}/start")
	public void startGame(@DestinationVariable int roomId) {
		Map<String, Object> map = new HashMap<>();
		map.put("roomId", roomId);
		map.put("status", "Y");

		miniGameService.setGameState(map);
		onlineGameservice.startGame(roomId);
	}

	// drawer 단어 선택
	@MessageMapping("/game/{roomId}/selectWord")
	public void selectWord(@DestinationVariable int roomId, @Payload Map<String, String> payload) {
		String answer = payload.get("answer");

		if (answer != null) {
			onlineGameservice.selectWord(roomId, answer);
		}
	}

	// ---------------- 그림판 ----------------
	@MessageMapping("/draw/{roomId}")
	public void draw(@DestinationVariable int roomId, DrawChunkMessage message) {
		onlineGameservice.handleDrawChunk(roomId, message);
	}

//	@MessageMapping("/draw/{roomId}")
//	public void handleDraw(@DestinationVariable int roomId, DrawMessage message) {
//		try {
//			log.info("메세지 내용 {}", message);
//			onlineGameservice.drawAndBroadCast(roomId, message);
//		} catch (Exception e) {
//			log.error("그림 데이터 처리 중 오류 발생: ", e);
//		}
//	}

	// 채팅
	@MessageMapping("/chat/{roomId}/sendMessage")
	public void handleChatMessage(@DestinationVariable int roomId, @Payload ChatMessage message) {
		onlineGameservice.checkAnswerAndBroadcast(roomId, message);
	}

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