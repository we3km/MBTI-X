package com.kh.mbtix.websoket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kh.mbtix.miniGame.model.dto.DrawChunkMessage; // ⭐ 추가: DTO import
import com.kh.mbtix.miniGame.model.service.OnlineGameService;

@Slf4j
@Component
public class DrawWebSocketHandler extends TextWebSocketHandler {

	private final Map<Integer, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	// ⭐ 추가: 청크 버퍼, 마지막 브로드캐스트 시간, 브로드캐스트 간격
	// (userId -> (chunkId -> (index -> chunkString)))
	private final Map<Integer, Map<String, Map<Integer, String>>> chunkBuffers = new ConcurrentHashMap<>();
	// (roomId -> lastBroadcastTimestamp)
	private final Map<Integer, Long> lastBroadcastTime = new ConcurrentHashMap<>();
	private static final long BROADCAST_INTERVAL_MS = 100; // 100ms (0.1초)

	public DrawWebSocketHandler(OnlineGameService onlineGameService) {
		scheduler.scheduleAtFixedRate(this::sendHeartbeat, 0, 500, TimeUnit.MILLISECONDS);
	}

	private void sendHeartbeat() {
		TextMessage heartbeatMessage = new TextMessage("heartbeat");
		roomSessions.values().forEach(sessions -> {
			sessions.forEach(session -> {
				if (session.isOpen()) {
					try {
						session.sendMessage(heartbeatMessage);
					} catch (IOException e) {
						log.error("Failed to send heartbeat to session {}: {}", session.getId(), e.getMessage());
						try {
							session.close(CloseStatus.SERVER_ERROR.withReason("Heartbeat send error"));
						} catch (IOException closeEx) {
							log.error("Error closing session after heartbeat error: {}", session.getId(), closeEx);
						}
					}
				}
			});
		});
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		// 쿼리 파라미터에서 roomId와 userId 추출
		Map<String, String> params = getQueryParameters(session.getUri().getQuery());
		String roomIdStr = params.get("roomId");
		String userIdStr = params.get("userId");

		if (roomIdStr == null || userIdStr == null) {
			log.warn("WebSocket session {} tried to connect without roomId or userId. Closing.", session.getId());
			session.close(CloseStatus.BAD_DATA.withReason("Missing roomId or userId query parameter"));
			return;
		}

		Integer roomId = Integer.parseInt(roomIdStr);
		Long userId = Long.parseLong(userIdStr); // userId도 파싱

		// 세션에 roomId와 userId 저장
		session.getAttributes().put("roomId", roomId);
		session.getAttributes().put("userId", userId);

		roomSessions.computeIfAbsent(roomId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(session);
		log.info("WebSocket session {} (User: {}, Room: {}) connected. Total sessions in room {}: {}", session.getId(),
				userId, roomId, roomId, roomSessions.get(roomId).size());
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		Integer roomId = (Integer) session.getAttributes().get("roomId");
		Long senderUserIdLong = (Long) session.getAttributes().get("userId");

		if (roomId == null || senderUserIdLong == null) {
			log.warn("Session {} has no roomId or userId attribute. Closing.", session.getId());
			session.close(CloseStatus.SERVER_ERROR.withReason("Room ID or User ID not found in session attributes"));
			return;
		}
		int senderUserId = senderUserIdLong.intValue(); // DrawChunkMessage의 userId는 int 타입이므로 변환

		// ⭐ 여기를 수정합니다: TextMessage의 페이로드를 DrawChunkMessage DTO로 파싱
		DrawChunkMessage drawChunkMessage = objectMapper.readValue(message.getPayload(), DrawChunkMessage.class);

		// 로깅 시 실제 보낸 유저와 DTO 내 userId가 일치하는지 확인 가능
		log.debug("Received drawing chunk (ID: {}, Index: {}) from actual user {} (DTO user {}) in room {}",
				drawChunkMessage.getId(), drawChunkMessage.getIndex(), senderUserId, drawChunkMessage.getUserId(),
				roomId);

		Map<String, Map<Integer, String>> userBuffer = chunkBuffers.computeIfAbsent(senderUserId,
				k -> new ConcurrentHashMap<>());
		Map<Integer, String> chunks = userBuffer.computeIfAbsent(drawChunkMessage.getId(),
				k -> new ConcurrentHashMap<>());

		chunks.put(drawChunkMessage.getIndex(), drawChunkMessage.getChunk());

		// 모든 조각이 도착했는지 확인합니다.
		if (chunks.size() == drawChunkMessage.getTotal()) {
			// ✅ 2. 도착한 모든 조각을 합쳐서 fullDataString을 만듭니다.
			StringBuilder fullDataString = new StringBuilder();
			for (int i = 0; i < drawChunkMessage.getTotal(); i++) {
				// 특정 인덱스에 청크가 없을 경우를 대비하여 null 체크 또는 예외 처리
				String chunkPart = chunks.get(i);
				if (chunkPart != null) {
					fullDataString.append(chunkPart);
				} else {
					log.warn("Missing chunk for ID {} at index {} in room {}", drawChunkMessage.getId(), i, roomId);
					// 누락된 청크가 있으면 이 스냅샷은 버리거나 부분적으로 처리할 수 있습니다.
					// 여기서는 일단 이 스냅샷은 브로드캐스팅하지 않고 넘어갑니다.
					userBuffer.remove(drawChunkMessage.getId()); // 불완전한 스냅샷 버퍼 제거
					return;
				}
			}

			// 3. 마지막으로 전송한 시간을 확인합니다.
			long currentTime = System.currentTimeMillis();
			long lastTime = lastBroadcastTime.getOrDefault(roomId, 0L);

			// ✅ 4. 전송 간격(100ms)이 지났는지 확인합니다.
			if (currentTime - lastTime > BROADCAST_INTERVAL_MS) {
				// log.info("[BROADCASTING] roomId: {}, assembledSize: {}", roomId,
				// fullDataString.length());

				// ✅ 5. 위에서 만든 fullDataString을 사용해 데이터를 전송합니다.
				// 이 메시지는 모든 클라이언트에게 전달되어야 합니다. (보낸 사람 제외)
				TextMessage fullSnapshotMessage = new TextMessage(fullDataString.toString());

				Set<WebSocketSession> sessionsInRoom = roomSessions.get(roomId);
				if (sessionsInRoom != null) {
					for (WebSocketSession s : sessionsInRoom) {
						// 보낸 유저와 다른 세션에만 메시지 전송
						if (s.isOpen() && !s.getId().equals(session.getId())) { // 세션 ID로 발신자 제외
							try {
								s.sendMessage(fullSnapshotMessage); // ⭐ 완성된 스냅샷 메시지 전달
							} catch (IOException e) {
								log.error("Failed to send full snapshot to session {}: {}", s.getId(), e.getMessage());
								s.close(CloseStatus.SERVER_ERROR.withReason("Full snapshot send error"));
							}
						}
					}
				}
				lastBroadcastTime.put(roomId, currentTime);
			} else {
				log.trace("[THROTTLED] Broadcasting skipped for roomId: {}", roomId);
			}

			// 6. 조립이 끝난 데이터는 메모리에서 즉시 삭제합니다.
			userBuffer.remove(drawChunkMessage.getId());
		}
		// ⭐⭐ 청크 재조립 및 스로틀링 로직 끝 ⭐⭐
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		Integer roomId = (Integer) session.getAttributes().get("roomId");
		Long userId = (Long) session.getAttributes().get("userId");

		if (roomId != null) {
			Set<WebSocketSession> sessionsInRoom = roomSessions.get(roomId);
			if (sessionsInRoom != null) {
				sessionsInRoom.remove(session);
				if (sessionsInRoom.isEmpty()) {
					roomSessions.remove(roomId);
				}
				log.info(
						"WebSocket session {} (User: {}, Room: {}) disconnected. Status: {}. Remaining sessions in room {}: {}",
						session.getId(), userId, roomId, status.getCode(), roomId, sessionsInRoom.size());
			}
		} else {
			log.info("WebSocket session {} disconnected. Status: {}. Room ID not found.", session.getId(),
					status.getCode());
		}
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage(), exception);
		if (session.isOpen()) {
			session.close(CloseStatus.SERVER_ERROR);
		}
	}

	// 쿼리 파라미터 파싱 유틸리티 함수
	private Map<String, String> getQueryParameters(String query) {
		Pattern pattern = Pattern.compile("([^&=]+)=([^&=]*)");
		Matcher matcher = pattern.matcher(query);
		Map<String, String> params = new ConcurrentHashMap<>();
		while (matcher.find()) {
			params.put(matcher.group(1), matcher.group(2));
		}
		return params;
	}
}