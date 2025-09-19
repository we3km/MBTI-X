package com.kh.mbtix.miniGame.model.service;

import com.kh.mbtix.miniGame.model.dto.ChatMessage;
import com.kh.mbtix.miniGame.model.dto.DrawChunkMessage;
import com.kh.mbtix.miniGame.model.dto.DrawMessage;

public interface OnlineGameService {
	public void startGame(int roomId);

	public void prepareRoom(int roomId);

	public void handleLeaveRoom(int roomId, int userId);

	public void selectWord(int roomId, String answer);

	public void checkAnswerAndBroadcast(int roomId, ChatMessage message);

	public void drawAndBroadCast(int roomId, DrawMessage message);

	public void handleDrawChunk(int roomId, DrawChunkMessage message);
}
