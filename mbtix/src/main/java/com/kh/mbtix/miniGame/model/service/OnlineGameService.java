package com.kh.mbtix.miniGame.model.service;

import com.kh.mbtix.miniGame.model.dto.ChatMessage;
import com.kh.mbtix.miniGame.model.dto.DrawChunkMessage;
import com.kh.mbtix.miniGame.model.dto.DrawMessage;
import com.kh.mbtix.miniGame.model.dto.GameRoomInfo;

public interface OnlineGameService {
	public void startGame(int roomId);

	public void prepareRoom(int roomId, int userId);

	public void selectWord(int roomId, String answer);

	public void checkAnswerAndBroadcast(int roomId, ChatMessage message);

	public void drawAndBroadCast(int roomId, DrawMessage message);

	public void handleDrawChunk(int roomId, DrawChunkMessage message);

	public void updateAndNotifyRoomInfo(GameRoomInfo updatedInfo);

	public void handleLeaveRoom(int roomId, int userId, int isKickedOut);
}
